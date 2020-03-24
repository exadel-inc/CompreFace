import logging
import sys
from pathlib import Path
from typing import Union, Callable

from flasgger import Swagger
from flask import Flask

from src.docs import DOCS_DIR
from src.endpoints import endpoints
from src.logging import init_logging
from src.services.flaskext.disable_caching import disable_caching
from src.services.flaskext.error_handling import add_error_handling
from src.services.flaskext.json_encoding import add_json_encoding
from src.cache import get_storage


def init_runtime():
    assert sys.version_info >= (3, 7)
    init_logging()
    logging.critical('d')
    get_storage().check_connection()


def create_app(add_endpoints_fun: Union[Callable, None] = None, docs_dir: Union[Path, None] = None):
    app = Flask('frs-core-ml')
    app.url_map.strict_slashes = False
    add_error_handling(app)
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
