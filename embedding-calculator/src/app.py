import logging
from typing import Union, Callable

from flask import Flask

from src import constants
from src._docs import add_docs
from src._endpoints import endpoints
from src.cache import get_storage
from src.constants import ENV
from src.docs import DOCS_DIR
from src.init_runtime import init_runtime
from src.services.flask_.disable_caching import disable_caching
from src.services.flask_.error_handling import add_error_handling
from src.services.flask_.json_encoding import add_json_encoding
from src.services.flask_.log_response import log_http_response

logger = logging.getLogger(__name__)


def init_app_runtime():
    init_runtime(logging_level=constants.LOGGING_LEVEL)
    logger.info(ENV.to_json() if ENV.IS_DEV_ENV else ENV.to_str())
    get_storage().wait_for_connection()


def create_app(add_endpoints_fun: Union[Callable, None] = None, do_add_docs: bool = False):
    app = Flask('frs-core-ml')
    app.url_map.strict_slashes = False
    add_error_handling(app)
    app.after_request(log_http_response)
    add_json_encoding(app)
    app.after_request(disable_caching)
    if do_add_docs:
        add_docs(app)
    if add_endpoints_fun:
        add_endpoints_fun(app)
    return app


def wsgi_app():
    init_app_runtime()
    logger.debug("Creating new app for WSGI")
    return create_app(endpoints, DOCS_DIR)


if __name__ == '__main__':
    init_app_runtime()
    app = create_app(endpoints, do_add_docs=True)
    app.config.from_mapping(SECRET_KEY='dev')
    app.run(host='0.0.0.0', port=ENV.ML_PORT, debug=True, use_debugger=False, use_reloader=False)
