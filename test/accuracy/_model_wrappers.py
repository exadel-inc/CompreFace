import logging
import random
import string
import time
from abc import ABC, abstractmethod
from http import HTTPStatus

import requests

from src.face_recognition.calc_embedding.calculator import calculate_embeddings
from src.classify_faces.predict import predict_from_image_with_classifier
from src.face_recognition.classify_embedding import get_trained_classifier
from src.face_recognition.crop_faces.crop_faces import crop_one_face
from src.face_recognition.crop_faces.exceptions import NoFaceFoundError
from src._pyutils.serialization import numpy_to_jpg_file
from test.test_perf.dto import Image, Name


class ModelWrapperBase(ABC):
    @abstractmethod
    def add_face_example(self, img: Image, name: Name):
        pass

    @abstractmethod
    def train(self):
        pass

    @abstractmethod
    def recognize(self, img: Image) -> Name:
        pass


class EfrsLocal(ModelWrapperBase):
    def __init__(self):
        self._cropped_images = []
        self._names = []
        self._classifier = None

    def add_face_example(self, img: Image, name: Name):
        try:
            cropped_img = crop_one_face(img).img
        except NoFaceFoundError as e:
            logging.warning(f"Failed to add face example. Skipping. {str(e)}")
            return 1
        self._cropped_images.append(cropped_img)
        self._names.append(name)
        return 0

    def train(self):
        embeddings = calculate_embeddings(self._cropped_images)
        self._classifier = get_trained_classifier(embeddings, self._names)

    def recognize(self, img: Image) -> Name:
        try:
            predictions = predict_from_image_with_classifier(img=img, classifier=self._classifier, limit=1)
        except NoFaceFoundError as e:
            logging.warning(f"Face is not found in the image to be recognized. Skipping. {str(e)}")
            return ''
        return predictions[0].face_name


class EfrsRestApi(ModelWrapperBase):
    TRAINING_TIMEOUT_S = 60 * 60 * 48

    @staticmethod
    def _random_string():
        return ''.join(random.choice(string.ascii_letters + string.digits) for _ in range(10))

    def __init__(self, host):
        self._host = host
        self._api_key = f"test-{self._random_string()}"

    def add_face_example(self, img: Image, name: Name):
        response = requests.post(f"{self._host}/faces/{name}?retrain=no",
                      headers={'X-Api-Key': self._api_key},
                      files={'file': numpy_to_jpg_file(img)})
        if response.status_code != 201:
            logging.warning(f"Failed to add face example. Skipping. {str(response.content)}")
            return 1
        return 0

    def train(self):
        requests.post(f"{self._host}/retrain", headers={'X-Api-Key': self._api_key})
        for _ in range(self.TRAINING_TIMEOUT_S):
            time.sleep(1)
            res = requests.get(f"{self._host}/retrain", headers={'X-Api-Key': self._api_key})
            if res.status_code == HTTPStatus.OK:
                return
        raise Exception("Waiting for classifier training completion has reached a timeout")

    def recognize(self, img: Image) -> Name:
        response = requests.post(f"{self._host}/recognize",
                                 headers={'X-Api-Key': self._api_key},
                                 files={'file': numpy_to_jpg_file(img)})
        if response.status_code != 200:
            logging.warning(f"Face is not found in the image to be recognized. Skipping. {str(response.content)}")
            return ''
        result = response.json()['result']
        return result[0]['face_name']
