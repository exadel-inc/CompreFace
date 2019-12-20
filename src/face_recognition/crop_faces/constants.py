from enum import IntEnum
from typing import Union

FACE_MIN_SIZE = 20
SCALE_FACTOR = 0.709
BOX_MARGIN = 32
IMAGE_SIZE = 160

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
