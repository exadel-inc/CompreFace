from abc import ABC, abstractmethod


class StorageBase(ABC):  # Ensures that child classes will override abstract methods
    @abstractmethod
    def add_face(self, raw_img, face_img, embedding, face_name, api_key):
        pass

    @abstractmethod
    def remove_face(self, api_key, face_name):
        pass

    @abstractmethod
    def get_embedding_calculator_model(self):
        pass

    @abstractmethod
    def get_classifier_training_data(self, api_key):
        pass

    @abstractmethod
    def get_api_keys(self):
        pass

    @abstractmethod
    def get_all_face_names(self, api_key):
        pass
