import logging
from collections import Callable
from http import HTTPStatus
from json import JSONEncoder
from pathlib import Path
from typing import Union

import numpy as np
from flasgger import Swagger
from flask import Flask, jsonify

from src.shared.api.exceptions import BadRequestException
from src.shared.utils.flaskutils import ConvertibleToDict


def create_app(add_endpoints_fun: Callable, docs_dir: Union[Path, None]):
    app = Flask(__name__)
    app.json_encoder = CustomJSONEncoder
    app.url_map.strict_slashes = False
    if docs_dir:
        app.config['SWAGGER'] = dict(title='EFRS - Swagger UI', doc_dir=str(docs_dir))
        Swagger(app, template_file=str(docs_dir / 'template.yml'))
    add_error_handling(app)
    app.after_request(disable_caching)
    add_endpoints_fun(app)

    @app.route('/status')
    def get_status():
        return jsonify(status="OK")

    return app


def add_error_handling(app: Flask):
    @app.errorhandler(BadRequestException)
    def handle_api_exception(e: BadRequestException):
        logging.warning(f'Response {e.http_status}: {str(e)}', exc_info=True)
        return jsonify(message=str(e)), e.http_status

    @app.errorhandler(Exception)
    def handle_runtime_error(e):
        logging.critical(f'Response 500: {str(e)}', exc_info=True)
        return jsonify(message=str(e)), HTTPStatus.INTERNAL_SERVER_ERROR


class CustomJSONEncoder(JSONEncoder):
    def default(self, obj):
        if isinstance(obj, ConvertibleToDict):
            return obj.to_dict()
        if isinstance(obj, np.ndarray):
            return obj.tolist()
        return super().default(obj)


def disable_caching(response):
    response.cache_control.max_age = 0
    response.cache_control.no_cache = True
    response.cache_control.no_store = True
    response.cache_control.must_revalidate = True
    response.cache_control.proxy_revalidate = True
    return response
