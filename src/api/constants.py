from enum import auto

from strenum import StrEnum

API_KEY_HEADER = 'X-Api-Key'


class GetParameter(StrEnum):
    RETRAIN = auto()
    FORCE = auto()


class RetrainValue(StrEnum):
    YES = auto()
    NO = auto()
    FORCE = auto()
