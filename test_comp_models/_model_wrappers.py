import logging
import random
import string
import time
from abc import ABC, abstractmethod
from http import HTTPStatus

import requests

from src.face_recognition.calc_embedding.calculator import calculate_embeddings
from src.face_recognition.classify_embedding.predict import predict_from_image_with_classifier
from src.face_recognition.classify_embedding.train import get_trained_classifier
from src.face_recognition.crop_faces.crop_faces import crop_one_face
from src.face_recognition.crop_faces.exceptions import NoFaceFoundError
from src.pyutils.serialization import numpy_to_jpg_file



class ModelWrapperBase(ABC):

    @abstractmethod
    def train(self, dataset):
        pass

    @abstractmethod
    def predict(self, dataset):
        pass


class EfrsLocal_2018(ModelWrapperBase):
    def __init__(self):
        self._cropped_images = []
        self._names = []
        self._classifier = None

    def train(self, dataset):

        undetected = 0
        for name in dataset:
            img = dataset[name]
            try:
                cropped_img = crop_one_face(img).img
                self._cropped_images.append(cropped_img)
                self._names.append(name)
            except NoFaceFoundError as e:
                logging.warning(f"Failed to add face example. Skipping. {str(e)}")
                undetected += 1

        embeddings = calculate_embeddings(self._cropped_images)
        self._classifier = get_trained_classifier(embeddings, self._names)
        return undetected

    def predict(self, dataset):
        recognised = 0
        for name in dataset:
            img = dataset[name]
            try:
                predictions = predict_from_image_with_classifier(img=img, classifier=self._classifier, limit=1)
                predicted_name = predictions[0].face_name
            except NoFaceFoundError as e:
                logging.warning(f"Face is not found in the image to be recognized. Skipping. {str(e)}")
                predicted_name = ""
            if predicted_name == name:
                recognised += 1

        return recognised


class EfrsLocal_InsightLib(ModelWrapperBase):

    def train(self, dataset):
        pass

    def predict(self, dataset):
        pass


class EfrsRestApi_2018(ModelWrapperBase):
    TRAINING_TIMEOUT_S = 60 * 60 * 48

    @staticmethod
    def _random_string():
        return ''.join(random.choice(string.ascii_letters + string.digits) for _ in range(10))

    def __init__(self, host):
        self._host = host
        self._api_key = f"test-{self._random_string()}"

    def train(self, dataset):
        undetected = 0
        for name in dataset:
            img = dataset[name]
            response = requests.post(f"{self._host}/faces/{name}?retrain=no",
                                     headers={'X-Api-Key': self._api_key},
                                     files={'file': numpy_to_jpg_file(img)})
            if response.status_code != 201:
                logging.warning(f"Failed to add face example. Skipping. {str(response.content)}")
                undetected += 1

        requests.post(f"{self._host}/retrain", headers={'X-Api-Key': self._api_key})
        for _ in range(self.TRAINING_TIMEOUT_S):
            time.sleep(1)
            res = requests.get(f"{self._host}/retrain", headers={'X-Api-Key': self._api_key})
            if res.status_code == HTTPStatus.OK:
                return undetected
        raise Exception("Waiting for classifier training completion has reached a timeout")

    def predict(self, dataset):
        recognized = 0
        for name in dataset:
            img = dataset[name]
            response = requests.post(f"{self._host}/recognize",
                                     headers={'X-Api-Key': self._api_key},
                                     files={'file': numpy_to_jpg_file(img)})
            if response.status_code != 200:
                logging.warning(f"Face is not found in the image to be recognized. Skipping. {str(response.content)}")
                return ''
            result = response.json()['result']
        if result[0]['face_name'] == name:
            recognized += 1
        return recognized


class EfrsRestApi_Insightlib(ModelWrapperBase):

    def train(self, dataset):
        pass

    def predict(self, dataset):
        pass
