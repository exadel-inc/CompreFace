from __future__ import annotations

from typing import List, Union
from typing import TYPE_CHECKING

from src import pyutils
from src.storage.dto.embedding_classifier import EmbeddingClassifier

if TYPE_CHECKING:
    from src.storage.dto.face import Face, FaceEmbedding
    from src.storage._database_wrapper.database_base import DatabaseBase


class _StorageBase:
    def __init__(self, database_wrapper: DatabaseBase):
        self._db = database_wrapper

    def get_api_keys(self) -> List[str]:
        return self._db.get_api_keys()

    def save_file(self, filename: str, bytes_data: bytes) -> None:
        return self._db.save_file(filename, bytes_data)

    def get_file(self, filename: str) -> bytes:
        return self._db.get_file(filename)


class Storage(_StorageBase):
    def with_key(self, api_key: str):
        return StorageWithKey(self._db, api_key)


class StorageWithKey(_StorageBase):
    def __init__(self, database_wrapper: DatabaseBase, api_key: str):
        super().__init__(database_wrapper)
        self._api_key = api_key

    def add_face(self, face: Face) -> None:
        self._db.add_face(self._api_key, face)

    def get_faces(self, calculator_version: str) -> List[Face]:
        return self._db.get_faces(self._api_key, calculator_version)

    def remove_face(self, face_name: str) -> None:
        self._db.remove_face(self._api_key, face_name)

    def get_face_names(self) -> List[str]:
        return self._db.get_face_names(self._api_key)

    def get_face_embeddings(self, calculator_version: str) -> List[FaceEmbedding]:
        return self._db.get_face_embeddings(self._api_key, calculator_version)

    def save_embedding_classifier(self, embedding_classifier: EmbeddingClassifier) -> None:
        self._db.save_embedding_classifier(self._api_key, embedding_classifier)

    def get_embedding_classifier(self, version: str, embedding_calculator_version: str) -> EmbeddingClassifier:
        return self._db.get_embedding_classifier(self._api_key, version, embedding_calculator_version)

    def delete_embedding_classifiers(self) -> None:
        self._db.delete_embedding_classifiers(self._api_key)


@pyutils.run_once
def _storage_singleton():
    from src.storage._database_wrapper.database_mongo import DatabaseMongo

    return Storage(DatabaseMongo())


def get_storage(api_key: str = None) -> Union[Storage, StorageWithKey]:
    storage = _storage_singleton()
    return storage.with_key(api_key) if api_key else storage
