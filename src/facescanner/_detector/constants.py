import os
from enum import IntEnum
from pathlib import Path
from typing import Union

FACE_MIN_SIZE = 20
SCALE_FACTOR = 0.709
BOX_MARGIN = 32

# There are three thresholds, each with different purpose in face detection ("Three step threshold")
DEFAULT_THRESHOLD_A = 0.9436513301
DEFAULT_THRESHOLD_B = 0.7059968943
DEFAULT_THRESHOLD_C = 0.5506904359


class FaceLimitConstant(IntEnum):
    """
    >>> bool(FaceLimitConstant.NO_LIMIT)  # Because implicit cast to bool is used for this constant
    False
    """
    NO_LIMIT = 0


FaceLimit = Union[FaceLimitConstant, int]  # Class for type hinting
CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
IMG_DIR = CURRENT_DIR / '_files'

