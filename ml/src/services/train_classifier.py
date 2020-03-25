import logging

import toolz

from src.cache import get_scanner, get_storage
from src.exceptions import NotEnoughUniqueFacesError
from src.loggingext import init_logging
from src.services.classifier.logistic_classifier import LogisticClassifier
from src.services.facescan.backend.facescan_backend import FacescanBackend
from src.services.storage.mongo_storage import MongoStorage


def get_faces(storage: MongoStorage, api_key: str, emb_calc_version: str):
    faces = storage.get_face_embeddings(api_key, emb_calc_version)
    unique_faces = list(toolz.unique(faces, lambda e: e.name))
    if len(unique_faces) <= 1:
        storage.delete_embedding_classifiers(api_key)
        raise NotEnoughUniqueFacesError
    return faces


def train_and_save_classifier(api_key: str) -> None:
    scanner: FacescanBackend = get_scanner()
    emb_calc_version = scanner.ID
    storage: MongoStorage = get_storage()
    faces = get_faces(storage, api_key, emb_calc_version)

    logging.debug("Started training classifier")
    embeddings = [face.embedding for face in faces]
    names = [face.name for face in faces]
    classifier = LogisticClassifier.train(embeddings, names, emb_calc_version)
    storage.save_embedding_classifier(api_key, classifier)
    logging.debug("Classifier trained and saved")


def train_and_save_classifier_async_task(api_key: str) -> None:
    init_logging()
    train_and_save_classifier(api_key)
