import logging
import os
import sys

from PIL import ImageFile

from src._logging import init_logging


def _check_jenkins_build_args():
    app_version_string = os.getenv('APP_VERSION_STRING', '')
    be_version = os.getenv('BE_VERSION', '')
    if app_version_string != be_version:
        logging.warning(f"APP_VERSION_STRING='{app_version_string}' "
                        f"and BE_VERSION='{be_version}' have different values")


def init_runtime(logging_level):
    assert sys.version_info >= (3, 7)
    ImageFile.LOAD_TRUNCATED_IMAGES = True
    _check_jenkins_build_args()
    init_logging(logging_level)
