from enum import IntEnum


class FaceLimitConstant(IntEnum):
    NO_LIMIT = 0


FACE_MIN_SIZE = 20
THRESHOLD = [0.6, 0.7, 0.7]  # three steps's threshold
SCALE_FACTOR = 0.709
MARGIN = 32
IMAGE_SIZE = 160
