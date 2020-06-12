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
import time
from enum import auto

import gridfs
import numpy as np
from pymongo import MongoClient
from pymongo.errors import ServerSelectionTimeoutError
from sklearn.linear_model import LogisticRegression
from strenum import StrEnum

from src.constants import ENV
from src.exceptions import NoTrainedEmbeddingClassifierFoundError, FaceHasNoEmbeddingCalculatedError, \
    CouldNotConnectToDatabase
from src.services.classifier.logistic_classifier import LogisticClassifier
from src.services.storage._serialization import serialize, deserialize
from src.services.storage.face import Face, FaceNameEmbedding
from src.services.storage.mongo_fileio import save_file_to_mongo, get_file_from_mongo

WAIT_FOR_CONNECTION_TIMEOUT_S = 60
logger = logging.getLogger(__name__)


class CollectionName(StrEnum):
    FACES = auto()
    CLASSIFIERS = auto()
    FILES = auto()


class MongoStorage:
    def __init__(self, host: str, port: int):
        self._mongodb_host = host
        self._mongodb_port = port
        self._mongodb = MongoClient(host=self._mongodb_host, port=self._mongodb_port)
        db = self._mongodb[ENV.MONGODB_DBNAME]
        self._faces_collection = db[CollectionName.FACES]
        self._faces_fs = gridfs.GridFS(db, CollectionName.FACES)
        self._classifiers_collection = db[CollectionName.CLASSIFIERS]
        self._classifiers_fs = gridfs.GridFS(db, CollectionName.CLASSIFIERS)
        self._files_fs = gridfs.GridFS(db, CollectionName.FILES)

    def wait_for_connection(self):
        start_time = time.time()
        logger.debug(f"Waiting for database connection at '{self._mongodb_host}:{self._mongodb_port}'")
        while True:
            try:
                self._mongodb.server_info()
                break
            except ServerSelectionTimeoutError as e:
                if time.time() - start_time > WAIT_FOR_CONNECTION_TIMEOUT_S:
                    raise CouldNotConnectToDatabase from e
                logger.debug(f"Retrying to get database connection at '{self._mongodb_host}:{self._mongodb_port}'")
                time.sleep(1)

    def add_face(self, api_key: str, face: Face, emb_calc_version: str):
        self._faces_collection.insert_one({
            "face_name": face.name,
            "embeddings": [
                {
                    "array": face.embedding.tolist(),
                    "calculator_version": emb_calc_version
                }
            ],
            "raw_img_fs_id": self._faces_fs.put(serialize(face.raw_img)),
            "face_img_fs_id": self._faces_fs.put(serialize(face.face_img)),
            "api_key": api_key
        })

    @staticmethod
    def _get_embedding(face_document, emb_calc_version):
        found_embeddings = [emb for emb in face_document['embeddings'] if
                            emb['calculator_version'] == emb_calc_version]
        if not found_embeddings:
            raise FaceHasNoEmbeddingCalculatedError
        return np.array(found_embeddings[0]['array'])

    def get_faces(self, api_key: str, emb_calc_version: str):
        faces = []
        for face_document in self._faces_collection.find({"api_key": api_key}):
            face_name = face_document['face_name']
            raw_img = deserialize(self._faces_fs.get(face_document['raw_img_fs_id']).read())
            face_img = deserialize(self._faces_fs.get(face_document['face_img_fs_id']).read())
            embedding = self._get_embedding(face_document, emb_calc_version)
            # noinspection PyTypeChecker
            faces.append(Face(name=face_name, embedding=embedding, raw_img=raw_img, face_img=face_img))
        return faces

    def remove_face(self, api_key: str, face_name: str):
        raw_images = self._faces_collection.find(filter={"face_name": face_name, "api_key": api_key},
                                                 projection={"raw_img_fs_id"}).distinct("raw_img_fs_id")
        for raw_img in raw_images:
            self._faces_fs.delete(raw_img)
        face_images = self._faces_collection.find(filter={"face_name": face_name, "api_key": api_key},
                                                  projection={"face_img_fs_id"}).distinct("face_img_fs_id")
        for face_img in face_images:
            self._faces_fs.delete(face_img)

        self._faces_collection.delete_many({'face_name': face_name, 'api_key': api_key})

    def get_face_names(self, api_key: str):
        return self._faces_collection.find(filter={"api_key": api_key},
                                           projection={"face_name": 1}).distinct("face_name")

    def get_face_embeddings(self, api_key: str, emb_calc_version: str):
        face_embeddings = []
        for face_document in self._faces_collection.find({"api_key": api_key}):
            face_name = face_document['face_name']
            embedding = self._get_embedding(face_document, emb_calc_version)
            face_embeddings.append(FaceNameEmbedding(name=face_name, embedding=embedding))
        return face_embeddings

    def save_embedding_classifier(self, api_key: str, embedding_classifier: LogisticClassifier):
        version = embedding_classifier.version
        emb_calc_version = embedding_classifier.emb_calc_version
        class_2_face_name = {str(k): v for k, v in embedding_classifier.class_2_face_name.items()}
        serialized_classifier = serialize(embedding_classifier.model)
        new_classifier_fs_id = self._classifiers_fs.put(serialized_classifier)

        document = self._classifiers_collection.find_one({
            'version': version,
            'embedding_calculator_version': emb_calc_version,
            "api_key": api_key
        })
        if document is not None:
            self._classifiers_fs.delete(document['classifier_fs_id'])

        self._classifiers_collection.update({
            'version': version,
            'embedding_calculator_version': emb_calc_version,
            "api_key": api_key
        }, {
            'version': version,
            'embedding_calculator_version': emb_calc_version,
            "api_key": api_key,
            "class_2_face_name": class_2_face_name,
            "classifier_fs_id": new_classifier_fs_id,
        }, upsert=True)

    def get_embedding_classifier(self, api_key, version, emb_calc_version):
        document = self._classifiers_collection.find_one({
            'version': version,
            'embedding_calculator_version': emb_calc_version,
            "api_key": api_key
        })
        if document is None:
            raise NoTrainedEmbeddingClassifierFoundError

        # noinspection PyTypeChecker
        model: LogisticRegression = deserialize(self._classifiers_fs.get(document['classifier_fs_id']).read())
        class_2_face_name = {int(k): v for k, v in document['class_2_face_name'].items()}
        return LogisticClassifier(model, class_2_face_name, emb_calc_version, version)

    def delete_embedding_classifiers(self, api_key):
        face_query = self._classifiers_collection.find(filter={"api_key": api_key}, projection={"classifier_fs_id"})
        for classifier_file in face_query.distinct("classifier_fs_id"):
            self._classifiers_fs.delete(classifier_file)
        self._classifiers_collection.delete_many({'api_key': api_key})

    def save_file(self, filename, bytes_data):
        save_file_to_mongo(self._files_fs, filename, bytes_data)

    def get_file(self, filename):
        return get_file_from_mongo(self._files_fs, filename)
