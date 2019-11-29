from enum import IntEnum
from typing import Union


class FaceLimitConstant(IntEnum):
    """
    >>> bool(FaceLimitConstant.NO_LIMIT)  # Because implicit cast to bool is used for this constant
    False
    """
    NO_LIMIT = 0


FaceLimit = Union[FaceLimitConstant, int]

class DetProbThresholdConstant(IntEnum):
    """
    >>> bool(DetProbThresholdConstant.NO_DET_PROB_THRESHOLD)  # Because implicit cast to bool is used for this constant
    False
    """
    NO_DET_PROB_THRESHOLD = 0


Detection_3rd_Threshold = Union[DetProbThresholdConstant, int]

FACE_MIN_SIZE = 20
DEFAULT_1ST_THRESHOLD = 0.9436513301
DEFAULT_2DN_THRESHOLD = .7059968943
DEFAULT_3RD_THRESHOLD = 0.5506904359  # three steps's threshold

SCALE_FACTOR = 0.709
MARGIN = 32
IMAGE_SIZE = 160
