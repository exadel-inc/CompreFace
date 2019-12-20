import logging
import os
from http import HTTPStatus
from pathlib import Path

import imageio
from flasgger import Swagger
from flask import jsonify, Flask
from flask.json import JSONEncoder

from src.api.endpoint_decorators import needs_attached_file
from src.api.exceptions import BadRequestException
from src.pyutils import ConvertibleToDict
from src.scan_faces import scan_faces, CALCULATOR_VERSION, DEFAULT_THRESHOLD_C, FaceLimitConstant

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
    def status_get():
        return jsonify(status="OK")

    @app.route('/scan_faces', methods=['POST'])
    @needs_attached_file
    def scan_faces_post():
        from flask import request
        try:
            face_limit = int(request.form.get('limit', FaceLimitConstant.NO_LIMIT))
            assert face_limit >= 0
        except ValueError as e:
            raise BadRequestException('Limit format is invalid') from e
        except AssertionError as e:
            raise BadRequestException('Limit value is invalid') from e
        try:
            detection_threshold_c = float(request.form.get('threshold_c', DEFAULT_THRESHOLD_C))
            assert 0 <= detection_threshold_c <= 1
        except AssertionError as e:
            raise BadRequestException('Threshold value is invalid') from e
        file = request.files['file']

        img = imageio.imread(file)
        scanned_faces = scan_faces(img, face_limit, detection_threshold_c)

        return jsonify(result=scanned_faces, calculator_version=CALCULATOR_VERSION)

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


def init_app():
    # Use create_app() for unit tests, use init_app() for actual running of
    # the server, so that additional server init steps can be done here.
    app = create_app()
    return app
