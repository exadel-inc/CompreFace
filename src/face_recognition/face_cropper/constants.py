from enum import IntEnum
from typing import Union


class FaceLimitConstant(IntEnum):
    """
    >>> bool(FaceLimitConstant.NO_LIMIT)  # Because implicit cast to bool is used for this constant
    False
    """
    NO_LIMIT = 0


FaceLimit = Union[FaceLimitConstant, int]

class ThresholdConstant(IntEnum):
    """
    >>> bool(FaceLimitConstant.NO_THRESHOLD)  # Because implicit cast to bool is used for this constant
    False
    """
    NO_THRESHOLD = 0


Threshold = Union[ThresholdConstant, int]

FACE_MIN_SIZE = 20
THRESHOLD = [0.9436513301, 0.7059968943, 0.5506904359]  # three steps's threshold
#THRESHOLD = [0.6, 0.7, 0.7]  # old threshold
SCALE_FACTOR = 0.709
MARGIN = 32
IMAGE_SIZE = 160
