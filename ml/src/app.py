import logging
import sys
from http import HTTPStatus
from pathlib import Path
from typing import Union, Callable

from flasgger import Swagger
from flask import Flask, Response

from src.constants import DO_SHOW_HTTP_RESPONSES_IN_LOGS
from src.docs import DOCS_DIR
from src.endpoints import endpoints
from src.loggingext import init_logging
from src.services.flaskext.disable_caching import disable_caching
from src.services.flaskext.error_handling import add_error_handling
from src.services.flaskext.json_encoding import add_json_encoding
from src.cache import get_storage
from src.services.flaskext.log_response import log_http_response


def init_runtime():
    assert sys.version_info >= (3, 7)
    init_logging()
    get_storage().check_connection()


def create_app(add_endpoints_fun: Union[Callable, None] = None, docs_dir: Union[Path, None] = None):
    app = Flask('frs-core-ml')
    app.url_map.strict_slashes = False
    add_error_handling(app)
    if DO_SHOW_HTTP_RESPONSES_IN_LOGS:
        app.after_request(log_http_response)
    add_json_encoding(app)
    app.after_request(disable_caching)
    if docs_dir:
        app.config['SWAGGER'] = dict(title='EFRS - Swagger UI', doc_dir=str(docs_dir))
        Swagger(app, template_file=str(docs_dir / 'template.yml'))
    if add_endpoints_fun:
        add_endpoints_fun(app)
    return app


def wsgi_app():
    init_runtime()
    logging.debug("Creating new app for WSGI")
    return create_app(endpoints, DOCS_DIR)


if __name__ == '__main__':
    init_runtime()
    app = create_app(endpoints, DOCS_DIR)
    app.config.from_mapping(SECRET_KEY='dev')
    app.run(host='0.0.0.0', port=3000, debug=True, use_debugger=False, use_reloader=False)
