from enum import auto

from strenum import StrEnum

API_KEY_HEADER = 'X-Api-Key'


class ARG:
    LIMIT = 'limit'
    DET_PROB_THRESHOLD = 'det_prob_threshold'


class GetParameter(StrEnum):
    RETRAIN = auto()
    FORCE = auto()


class RetrainValue(StrEnum):
    YES = auto()
    NO = auto()
    FORCE = auto()
