import logging
from multiprocessing import Process
from typing import Dict, Callable

import toolz
from toolz import valfilter

from src.exceptions import ClassifierIsAlreadyTrainingError
from src.runtime import get_storage, get_scanner
from src.services.classifier.logistic_classifier import LogisticClassifier


def train_and_save_classifier(api_key: str) -> None:
    emb_calc_version = get_scanner().ID
    faces = get_storage().get_face_embeddings(api_key, emb_calc_version)
    unique_faces = list(toolz.unique(faces, lambda e: e.name))
    if len(unique_faces) <= 1:
        logging.warning("Not enough unique faces to start training a new classifier model. "
                        "Deleting existing classifiers, if any.")
        get_storage().delete_embedding_classifiers()
        return

    logging.debug("Started training classifier for api_key, '%s'", api_key)
    embeddings = [face.embedding for face in faces]
    names = [face.name for face in faces]
    classifier = LogisticClassifier.train(embeddings, names, emb_calc_version)
    get_storage().save_embedding_classifier(api_key, classifier)
    logging.debug("Classifier trained and saved for api_key, '%s'", api_key)


class TrainingTaskManager:
    def __init__(self, train_fun: Callable[[str], None]):
        # noinspection PyPep8Naming
        ApiKey = str
        self._dict: Dict[ApiKey, 'Process'] = {}
        self._train_fun = train_fun

    def is_training(self, api_key):
        return api_key in self.dict

    def start_training(self, api_key, force=False):
        if force:
            self.abort_training(api_key)
        elif not self.is_training(api_key):
            raise ClassifierIsAlreadyTrainingError

        process = Process(target=self._train_fun, daemon=True, args=(api_key,))
        process.start()
        self._dict[api_key] = process

    def abort_training(self, api_key):
        if not self.is_training(api_key):
            return
        self._dict[api_key].terminate()

    @property
    def dict(self):
        self._dict = valfilter(lambda process: process.is_alive(), self._dict)
        return self._dict
