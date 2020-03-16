import logging
import os
from http import HTTPStatus
from pathlib import Path

import imageio
from flasgger import Swagger
from flask import jsonify, Response, Flask
from flask.json import JSONEncoder

from src._pyutils.convertible_to_dict import ConvertibleToDict
from src.api.constants import API_KEY_HEADER, GetParameter
from src.api.endpoint_decorators import needs_authentication, needs_attached_file, needs_retrain
from src.api.exceptions import BadRequestException
from src.api.parse_request_arg import parse_request_bool_arg
from src.api.training_task_manager import start_training, is_training, abort_training
from src.classifier.predict import predict_from_image_with_api_key
from src.facescanner._detector.constants import FaceLimitConstant, DEFAULT_THRESHOLD_C
from src.facescanner.facescanner import scan_face
from src.storage.dto.face import Face
from src.storage.storage import get_storage
from PIL import ImageFile

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
        detection_threshold_c = float(request.values.get('det_prob_threshold', DEFAULT_THRESHOLD_C))

        ImageFile.LOAD_TRUNCATED_IMAGES = True
        img = imageio.imread(file)

        face = scan_face(img, detection_threshold_c=detection_threshold_c)
        get_storage(api_key).add_face(Face(name=face_name, raw_img=img, face_img=face.img, embedding=face.embedding))

        return Response(status=HTTPStatus.CREATED)

    @app.route('/faces/<face_name>', methods=['DELETE'])
    @needs_authentication
    @needs_retrain
    def remove_face(face_name):
        from flask import request
        api_key = request.headers[API_KEY_HEADER]

        get_storage(api_key).remove_face(face_name)

        return Response(status=HTTPStatus.NO_CONTENT)

    @app.route('/retrain', methods=['GET'])
    @needs_authentication
    def retrain_model_status():
        from flask import request
        api_key = request.headers[API_KEY_HEADER]

        it_is_training = is_training(api_key)

        return Response(status=HTTPStatus.ACCEPTED if it_is_training else HTTPStatus.OK)

    @app.route('/retrain', methods=['POST'])
    @needs_authentication
    def retrain_model_start():
        from flask import request
        api_key = request.headers[API_KEY_HEADER]
        force_start = parse_request_bool_arg(name=GetParameter.FORCE, default=False, request=request)

        start_training(api_key, force_start)

        return Response(status=HTTPStatus.ACCEPTED)

    @app.route('/retrain', methods=['DELETE'])
    @needs_authentication
    def retrain_model_abort():
        from flask import request
        api_key = request.headers[API_KEY_HEADER]

        abort_training(api_key)

        return Response(status=HTTPStatus.NO_CONTENT)

    @app.route('/recognize', methods=['POST'])
    @needs_authentication
    @needs_attached_file
    def recognize_faces():
        from flask import request
        try:
            limit = int(request.values.get('limit', FaceLimitConstant.NO_LIMIT))
            detection_threshold_c = float(request.values.get('det_prob_threshold', DEFAULT_THRESHOLD_C))
            assert limit >= 0
        except ValueError as e:
            raise BadRequestException('Limit format is invalid') from e
        except AssertionError as e:
            raise BadRequestException('Limit value is invalid') from e
        api_key = request.headers[API_KEY_HEADER]
        file = request.files['file']

        ImageFile.LOAD_TRUNCATED_IMAGES = True
        img = imageio.imread(file)
        face_predictions = predict_from_image_with_api_key(img, limit, api_key, detection_threshold_c)

        return jsonify(result=face_predictions)

    @app.errorhandler(BadRequestException)
    def handle_api_exception(e: BadRequestException):
        logging.warning(f'Response {e.http_status}: {str(e)}', exc_info=True)
        return jsonify(message=str(e)), e.http_status

    @app.errorhandler(Exception)
    def handle_runtime_error(e):
        if e.code != HTTPStatus.INTERNAL_SERVER_ERROR:
            logging.warning(f'Response {e.code}: {str(e)}', exc_info=True)
            return jsonify(message=str(e)), e.code
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


def init_app():
    # Use create_app() for unit tests, use init_app() for actual running of
    # the server, so that additional server init steps can be done here.
    app = create_app()
    return app
