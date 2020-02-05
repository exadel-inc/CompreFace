import gridfs
import numpy as np
from pymongo import MongoClient

from src.face_recognition.dto.embedding import Embedding
from src.pyutils.serialization import deserialize, serialize
from src.storage._database_wrapper.database_base import DatabaseBase
from src.storage._database_wrapper.mongo_fileio import save_file_to_mongo, get_file_from_mongo
from src.storage.constants import MONGO_EFRS_DATABASE_NAME, MONGO_HOST, MONGO_PORT, CollectionName
from src.storage.dto.embedding_classifier import EmbeddingClassifier
from src.storage.dto.face import Face, FaceEmbedding
from src.storage.exceptions import FaceHasNoEmbeddingSavedError, NoTrainedEmbeddingClassifierFoundError
import logging

class DatabaseMongo(DatabaseBase):
    def __init__(self):
        self._mongo_client = MongoClient(host=MONGO_HOST, port=MONGO_PORT)
        db = self._mongo_client[MONGO_EFRS_DATABASE_NAME]
        self._faces_collection = db[CollectionName.FACES]
        self._faces_fs = gridfs.GridFS(db, CollectionName.FACES)
        self._classifiers_collection = db[CollectionName.CLASSIFIERS]
        self._classifiers_fs = gridfs.GridFS(db, CollectionName.CLASSIFIERS)
        self._files_fs = gridfs.GridFS(db, CollectionName.FILES)

    def add_face(self, api_key, face):
        self._faces_collection.insert_one({
            "face_name": face.name,
            "embeddings": [
                {
                    "array": face.embedding.array.tolist(),
                    "calculator_version": face.embedding.calculator_version
                }
            ],
            "raw_img_fs_id": self._faces_fs.put(serialize(face.raw_img)),
            "face_img_fs_id": self._faces_fs.put(serialize(face.face_img)),
            "api_key": api_key
        })

    def _get_faces_iterator(self, api_key):
        return self._faces_collection.find({"api_key": api_key})

    @staticmethod
    def _document_to_embedding(document, calculator_version):
        found_embeddings = [emb for emb in document['embeddings'] if emb['calculator_version'] == calculator_version]
        if not found_embeddings:
            raise FaceHasNoEmbeddingSavedError

        found_embedding = found_embeddings[0]
        return Embedding(array=np.asarray(found_embedding['array']),
                         calculator_version=found_embedding['calculator_version'])

    def get_faces(self, api_key, calculator_version):
        def document_to_face(document):
            return Face(
                name=document['face_name'],
                embedding=self._document_to_embedding(document, calculator_version),
                raw_img=deserialize(self._faces_fs.get(document['raw_img_fs_id']).read()),
                face_img=deserialize(self._faces_fs.get(document['face_img_fs_id']).read())
            )

        return [document_to_face(document) for document in self._get_faces_iterator(api_key)]

    def remove_face(self, api_key, face_name):

        img_query = self._faces_collection.find(filter={"api_key": api_key, "face_name": face_name}, projection={"raw_img_fs_id"})
        face_query = self._faces_collection.find(filter={"api_key": api_key, "face_name": face_name}, projection={"face_img_fs_id"})
        if len(img_query.distinct("raw_img_fs_id")) > 0 and len(face_query.distinct("face_img_fs_id")) > 0:
            img = img_query.distinct("raw_img_fs_id")[0]
            face = face_query.distinct("face_img_fs_id")[0]
        else:
            logging.debug(f'File with filename {face_name} is not found in the database')
            return
        self._faces_collection.delete_many({'face_name': face_name, 'api_key': api_key})
        self._faces_fs.delete(img)
        self._faces_fs.delete(face)

    def get_face_names(self, api_key):
        find_query = self._faces_collection.find(filter={"api_key": api_key}, projection={"face_name": 1})
        return find_query.distinct("face_name")

    def get_face_embeddings(self, api_key, calculator_version):
        def document_to_face_embedding(document):
            return FaceEmbedding(
                name=document['face_name'],
                embedding=self._document_to_embedding(document, calculator_version)
            )

        return [document_to_face_embedding(document) for document in self._get_faces_iterator(api_key)]

    def save_embedding_classifier(self, api_key, embedding_classifier):
        self._classifiers_collection.update({
            'version': embedding_classifier.version,
            'embedding_calculator_version': embedding_classifier.embedding_calculator_version,
            "api_key": api_key
        }, {
            'version': embedding_classifier.version,
            'embedding_calculator_version': embedding_classifier.embedding_calculator_version,
            "api_key": api_key,
            "class_2_face_name": {str(k): v for k, v in embedding_classifier.class_2_face_name.items()},
            "classifier_fs_id": self._classifiers_fs.put(serialize(embedding_classifier.model))
        }, upsert=True)

    def get_embedding_classifier(self, api_key, version, embedding_calculator_version):
        document = self._classifiers_collection.find_one({
            'version': version,
            'embedding_calculator_version': embedding_calculator_version,
            "api_key": api_key
        })
        if document is None:
            raise NoTrainedEmbeddingClassifierFoundError(f"No classifier model is yet trained for API key '{api_key}'")

        model = deserialize(self._classifiers_fs.get(document['classifier_fs_id']).read())
        class_2_face_name = {int(k): v for k, v in document['class_2_face_name'].items()}
        return EmbeddingClassifier(version, model, class_2_face_name, embedding_calculator_version)

    def delete_embedding_classifiers(self, api_key):
        face_query = self._classifiers_collection.find(filter={"api_key": api_key}, projection = {"classifier_fs_id"})
        face = face_query.distinct("classifier_fs_id")[0]
        self._classifiers_collection.delete_many({'api_key': api_key})
        self._classifiers_fs.delete(face)

    def get_api_keys(self):
        return self._faces_collection.find(projection=["api_key"]).distinct("api_key")

    def save_file(self, filename, bytes_data):
        save_file_to_mongo(self._files_fs, filename, bytes_data)

    def get_file(self, filename):
        return get_file_from_mongo(self._files_fs, filename)
