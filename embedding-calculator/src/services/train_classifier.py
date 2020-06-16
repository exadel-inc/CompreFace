#  Copyright (c) 2020 the original author or authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

import logging

import toolz

from src import constants
from src.cache import get_scanner, get_storage
from src.exceptions import NotEnoughUniqueFacesError
from src.init_runtime import init_runtime
from src.services.classifier.logistic_classifier import LogisticClassifier
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.storage.mongo_storage import MongoStorage

logger = logging.getLogger(__name__)


def get_faces(storage: MongoStorage, api_key: str, emb_calc_version: str):
    faces = storage.get_face_embeddings(api_key, emb_calc_version)
    unique_faces = list(toolz.unique(faces, lambda e: e.name))
    if len(unique_faces) <= 1:
        storage.delete_embedding_classifiers(api_key)
        raise NotEnoughUniqueFacesError
    return faces


def train_and_save_classifier(api_key: str) -> None:
    scanner: FaceScanner = get_scanner()
    emb_calc_version = scanner.ID
    storage: MongoStorage = get_storage()
    faces = get_faces(storage, api_key, emb_calc_version)

    logger.debug("Started training classifier")
    embeddings = [face.embedding for face in faces]
    names = [face.name for face in faces]
    classifier = LogisticClassifier.train(embeddings, names, emb_calc_version)
    storage.save_embedding_classifier(api_key, classifier)
    logger.debug("Classifier trained and saved")


def train_and_save_classifier_async(api_key: str) -> None:
    init_runtime(logging_level=constants.LOGGING_LEVEL)
    # noinspection PyBroadException
    try:
        return train_and_save_classifier(api_key)
    except Exception as e:
        logger.error(f"Failed to train and save classifier: {str(e)}")
        exit(1)
