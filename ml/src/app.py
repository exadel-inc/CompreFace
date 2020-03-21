import logging
from http import HTTPStatus
from json import JSONEncoder
from pathlib import Path
from typing import Union, Callable

import numpy as np
from flasgger import Swagger
from flask import Flask, jsonify
from werkzeug.exceptions import HTTPException


def create_app(add_endpoints_fun: Union[Callable, None] = None, docs_dir: Union[Path, None] = None):
    app = Flask(__name__)
    app.url_map.strict_slashes = False

    class AppJSONEncoder(JSONEncoder):
        def default(self, obj):
            if isinstance(obj, JSONEncodable):
                return obj.to_json()
            if isinstance(obj, np.ndarray):
                return obj.tolist()
            return super().default(obj)

    app.json_encoder = AppJSONEncoder

    @app.after_request
    def _disable_caching(response):
        response.cache_control.max_age = 0
        response.cache_control.no_cache = True
        response.cache_control.no_store = True
        response.cache_control.must_revalidate = True
        response.cache_control.proxy_revalidate = True
        return response

    if docs_dir:
        app.config['SWAGGER'] = dict(title='EFRS - Swagger UI', doc_dir=str(docs_dir))
        Swagger(app, template_file=str(docs_dir / 'template.yml'))

    @app.errorhandler(HTTPException)
    def handle_http_exception(e: HTTPException):
        logging.warning(str(e), exc_info=True)
        return jsonify(message=str(e)), e.code

    @app.errorhandler(Exception)
    def handle_exception(e):
        msg = f"{e.__class__.__name__}{f': {str(e)}' if str(e) else ''}"
        logging.critical(msg, exc_info=True)
        return jsonify(message=msg), HTTPStatus.INTERNAL_SERVER_ERROR

    if add_endpoints_fun:
        add_endpoints_fun(app)

    return app


class JSONEncodable:
    def to_json(self):
        if hasattr(self, 'dto'):
            return self.dto.to_json()
        return self.__dict__
