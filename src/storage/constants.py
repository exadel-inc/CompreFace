import os
from enum import auto

from strenum import StrEnum

from src.facescanner._embedder.constants import EMBEDDING_CALCULATOR_MODEL_FILENAME, CALCULATOR_VERSION

EMBEDDING_CALCULATOR_MODEL_FILENAME = EMBEDDING_CALCULATOR_MODEL_FILENAME
CALCULATOR_VERSION = CALCULATOR_VERSION
MONGO_EFRS_DATABASE_NAME = "efrs_db"
MONGO_HOST = os.environ.get('MONGO_HOST', 'mongo')
MONGO_PORT = int(os.environ.get('MONGO_PORT', '27017'))


class CollectionName(StrEnum):
    FACES = auto()
    CLASSIFIERS = auto()
    FILES = auto()
