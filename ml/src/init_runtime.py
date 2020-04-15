import os
import sys

import imageio
from PIL import ImageFile

from src._logging import init_logging


def _apply_imageio_fixes():
    if os.environ.get('HOME') == '/root':  # TODO remove
        os.environ['HOME'] = '/srv'  # TODO remove
    imageio.plugins.freeimage.download()  # TODO remove
    ImageFile.LOAD_TRUNCATED_IMAGES = True


def init_runtime(logging_level):
    assert sys.version_info >= (3, 7)
    _apply_imageio_fixes()
    init_logging(logging_level)
