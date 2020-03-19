import logging
import sys

from PIL import ImageFile

from src.app import create_app
from src.docs.docs_dir import DOCS_DIR
from src.endpoints import endpoints


def _init():
    assert sys.version_info >= (3, 7)
    ImageFile.LOAD_TRUNCATED_IMAGES = True
    logging.basicConfig(level=logging.DEBUG)


def wsgi_app():
    _init()
    return create_app(endpoints, DOCS_DIR)


if __name__ == '__main__':
    _init()
    app = create_app(endpoints, DOCS_DIR)
    app.config.from_mapping(SECRET_KEY='dev')
    app.run(host='0.0.0.0', port=3000, debug=True, use_debugger=False, use_reloader=False)
