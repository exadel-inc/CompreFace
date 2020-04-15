import sys

import imageio
from PIL import ImageFile

from src._logging import init_logging


def init_runtime(logging_level):
    assert sys.version_info >= (3, 7)
    imageio.plugins.freeimage.download()
    ImageFile.LOAD_TRUNCATED_IMAGES = True
    init_logging(logging_level)
