from enum import auto

from strenum import StrEnum

API_KEY_HEADER = 'X-Api-Key'


class GET_PARAM(StrEnum):
    RETRAIN = auto()
    FORCE = auto()


class RETRAIN_VALUES(StrEnum):
    YES = auto()
    NO = auto()
    FORCE = auto()
