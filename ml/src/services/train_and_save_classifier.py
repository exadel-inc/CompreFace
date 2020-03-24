import logging

import toolz

from src.services.classifier.logistic_classifier import LogisticClassifier
from src.services.facescan.backend.facescan_backend import FacescanBackend
from src.services.storage.mongo_storage import MongoStorage
from src.cache import get_scanner, get_storage


def train_and_save_classifier(api_key: str) -> None:
    try:
        scanner: FacescanBackend = get_scanner()
        emb_calc_version = scanner.ID
        storage: MongoStorage = get_storage()
        faces = storage.get_face_embeddings(api_key, emb_calc_version)
        unique_faces = list(toolz.unique(faces, lambda e: e.name))
        if len(unique_faces) <= 1:
            logging.warning("Not enough unique faces to start training a new classifier model. "
                            "Deleting existing classifiers, if any.")
            storage.delete_embedding_classifiers(api_key)
            return

        logging.debug("Started training classifier for api_key, '%s'", api_key)
        embeddings = [face.embedding for face in faces]
        names = [face.name for face in faces]
        classifier = LogisticClassifier.train(embeddings, names, emb_calc_version)
        storage.save_embedding_classifier(api_key, classifier)
        logging.debug("Classifier trained and saved for api_key, '%s'", api_key)
    except Exception as e:
        logging.error('Could not train and save classifier')
        raise e from None
