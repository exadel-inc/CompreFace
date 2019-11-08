import logging
import os
from http import HTTPStatus
from pathlib import Path

import imageio
from flasgger import Swagger
from flask import jsonify, Response, Flask
from flask.json import JSONEncoder

from src.api._decorators import needs_authentication, needs_attached_file, needs_retrain
from src.api.constants import API_KEY_HEADER
from src.api.exceptions import BadRequestException
from src.face_recognition.embedding_classifier.predict import predict_from_image
from src.face_recognition.embedding_classifier.train import train_async
from src.face_recognition.face_cropper.constants import FaceLimitConstant
from src.pyutils.convertible_to_dict import ConvertibleToDict
from src.storage.dto.face import Face
from src.storage.storage import get_storage

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
DOCS_DIR = CURRENT_DIR / 'docs'


class DictJSONEncoder(JSONEncoder):
    def default(self, obj):
        if isinstance(obj, ConvertibleToDict):
            return obj.to_dict()
        return super().default(obj)


def create_app():
    app = Flask(__name__)
    app.json_encoder = DictJSONEncoder
    app.url_map.strict_slashes = False
    app.config['SWAGGER'] = dict(title='EFRS - Swagger UI', doc_dir=str(DOCS_DIR))
    Swagger(app, template_file=str(DOCS_DIR / 'template.yml'))

    @app.route('/status')
    def get_status():
        return jsonify(status="OK")

    @app.route('/faces')
    @needs_authentication
    def list_faces():
        from flask import request
        api_key = request.headers[API_KEY_HEADER]

        face_names = get_storage(api_key).get_face_names()

        return jsonify(names=face_names)

    @app.route('/faces/<face_name>', methods=['POST'])
    @needs_authentication
    @needs_attached_file
    @needs_retrain
    def add_face(face_name):
        from flask import request
        file = request.files['file']
        api_key = request.headers[API_KEY_HEADER]
        img = imageio.imread(file)

        face = Face.from_image(face_name, img)
        get_storage(api_key).add_face(face)

        return Response(status=HTTPStatus.CREATED)

    @app.route('/faces/<face_name>', methods=['DELETE'])
    @needs_authentication
    @needs_retrain
    def remove_face(face_name):
        from flask import request
        api_key = request.headers[API_KEY_HEADER]

        get_storage(api_key).remove_face(face_name)

        return Response(status=HTTPStatus.NO_CONTENT)

    @app.route('/retrain', methods=['POST'])
    @needs_authentication
    def retrain_model():
        from flask import request
        api_key = request.headers[API_KEY_HEADER]

        train_thread = train_async(api_key)
        # TODO EFRS-42 Remove this temporary 'await' parameter once there is an official way for E2E tests to wait for the training to finish
        if request.args.get('await', '').lower() in ('true', '1'):
            train_thread.join()

        return Response(status=HTTPStatus.ACCEPTED)

    @app.route('/recognize', methods=['POST'])
    @needs_authentication
    @needs_attached_file
    def recognize_faces():
        from flask import request
        try:
            limit = int(request.values.get('limit', FaceLimitConstant.NO_LIMIT))
            assert limit >= 0
        except ValueError as e:
            raise BadRequestException('Limit format is invalid') from e
        except AssertionError as e:
            raise BadRequestException('Limit value is invalid') from e
        api_key = request.headers[API_KEY_HEADER]
        file = request.files['file']

        img = imageio.imread(file)
        face_predictions = predict_from_image(img, limit, api_key)

        return jsonify(result=face_predictions)

    @app.errorhandler(BadRequestException)
    def handle_api_exception(e: BadRequestException):
        logging.warning(f'Response {e.http_status}: {str(e)}', exc_info=True)
        return jsonify(message=str(e)), e.http_status

    @app.errorhandler(Exception)
    def handle_runtime_error(e):
        logging.critical(f'Response 500: {str(e)}', exc_info=True)
        return jsonify(message=str(e)), HTTPStatus.INTERNAL_SERVER_ERROR

    @app.after_request
    def disable_caching(response):
        response.cache_control.max_age = 0
        response.cache_control.no_cache = True
        response.cache_control.no_store = True
        response.cache_control.must_revalidate = True
        response.cache_control.proxy_revalidate = True
        return response

    return app


def init_runtime():
    logging.basicConfig(level=logging.DEBUG)


def init_app():
    init_runtime()
    app = create_app()
    return app
