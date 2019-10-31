from typing import List, Union

from src import pyutils
from src.storage._database_wrapper.database_base import DatabaseBase
from src.storage._database_wrapper.database_mongo import DatabaseMongo
from src.storage.dto.embedding_classifier import EmbeddingClassifier
from src.storage.dto.face import Face, FaceEmbedding
from src.storage.exceptions import NoTrainedEmbeddingClassifierFoundError, FileNotFound


class _StorageBase:
    def __init__(self, database_wrapper: DatabaseBase):
        self._db = database_wrapper

    def get_api_keys(self) -> List[str]:
        return self._db.get_api_keys()

    def save_file(self, filename: str, bytes_data: bytes) -> None:
        return self._db.save_file(filename, bytes_data)

    def get_file(self, filename: str) -> bytes:
        bytes_data = self._db.get_file(filename)
        if bytes_data is None:
            raise FileNotFound(f'File with filename {filename} is not found in the database')
        return bytes_data


class Storage(_StorageBase):
    def with_key(self, api_key: str):
        return StorageWithKey(self._db, api_key)


class StorageWithKey(_StorageBase):
    def __init__(self, database_wrapper: DatabaseBase, api_key: str):
        super().__init__(database_wrapper)
        self._api_key = api_key

    def add_face(self, face: Face) -> None:
        self._db.add_face(self._api_key, face)

    def get_faces(self) -> List[Face]:
        return self._db.get_faces(self._api_key)

    def remove_face(self, face_name: str) -> None:
        self._db.remove_face(self._api_key, face_name)

    def get_face_names(self, api_key: str) -> List[str]:
        return self._db.get_face_names(api_key)

    def get_face_embeddings(self) -> List[FaceEmbedding]:
        return self._db.get_face_embeddings(self._api_key)

    def save_embedding_classifier(self, api_key: str, embedding_classifier: EmbeddingClassifier) -> None:
        self._db.save_embedding_classifier(api_key, embedding_classifier)

    def get_embedding_classifier(self, api_key: str) -> EmbeddingClassifier:
        classifier = self._db.get_embedding_classifier(api_key)
        if classifier is None:
            msg = 'No trained classifier model is found for the api_key %s'
            raise NoTrainedEmbeddingClassifierFoundError(msg, api_key)
        return classifier

    def delete_embedding_classifier(self, api_key: str) -> None:
        self._db.delete_embedding_classifier(api_key)


@pyutils.run_once
def _storage_singleton():
    return Storage(DatabaseMongo())


def get_storage(api_key: str = None) -> Union[Storage, StorageWithKey]:
    storage = _storage_singleton()
    return storage.with_key(api_key) if api_key else storage
