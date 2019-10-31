from abc import ABC, abstractmethod
from typing import List, Union

from src.storage.dto.embedding_classifier import EmbeddingClassifier
from src.storage.dto.face import Face, FaceEmbedding


class DatabaseBase(ABC):  # Ensures that child classes will override abstract methods
    @abstractmethod
    def add_face(self, api_key: str, face: Face) -> None:
        pass

    @abstractmethod
    def get_faces(self, api_key: str) -> List[Face]:
        pass

    @abstractmethod
    def remove_face(self, api_key: str, face_name: str) -> None:
        pass

    @abstractmethod
    def get_face_names(self, api_key: str) -> List[str]:
        pass

    @abstractmethod
    def get_face_embeddings(self, api_key: str) -> List[FaceEmbedding]:
        pass

    @abstractmethod
    def save_embedding_classifier(self, api_key: str, embedding_classifier: EmbeddingClassifier) -> None:
        pass

    @abstractmethod
    def get_embedding_classifier(self, api_key: str, name: str, embedding_calculator_name: str) -> Union[EmbeddingClassifier, None]:
        pass

    @abstractmethod
    def delete_embedding_classifiers(self, api_key: str) -> None:
        pass

    @abstractmethod
    def get_api_keys(self) -> List[str]:
        pass

    @abstractmethod
    def save_file(self, filename: str, bytes_data: bytes) -> None:
        pass

    @abstractmethod
    def get_file(self, filename: str) -> Union[bytes, None]:
        pass
