import os

import gridfs
from pymongo import MongoClient

from src.face_recognition.dto.face_embedding import Embedding
from src.pyutils.serialization import deserialize, serialize
from src.storage._database_wrapper.database_base import DatabaseBase
from src.storage.constants import EMBEDDING_CALCULATOR_MODEL_FILENAME
from src.storage.dto.embedding_classifier import EmbeddingClassifier
from src.storage.dto.face import Face, FaceEmbedding
from src.storage.exceptions import FaceHasNoEmbeddingSavedError


class NAMES:
    DATABASE = "recognition"  # TODO RENAME "EFRS_DB"

    class COLLECTION:
        FACES = 'faces'
        CLASSIFIERS = 'classifiers'
        FILES = 'models'  # TODO RENAME "files"


class DatabaseMongo(DatabaseBase):
    def __init__(self):
        self._mongo_client = MongoClient(host=os.environ.get('MONGO_HOST', 'mongo'),
                                         port=int(os.environ.get('MONGO_PORT', '27017')))
        db = self._mongo_client[NAMES.DATABASE]
        self._faces_collection = db[NAMES.COLLECTION.FACES]
        self._faces_fs = gridfs.GridFS(db, NAMES.COLLECTION.FACES)
        self._classifiers_collection = db[NAMES.COLLECTION.CLASSIFIERS]
        self._classifiers_fs = gridfs.GridFS(db, NAMES.COLLECTION.CLASSIFIERS)
        self._files_fs = gridfs.GridFS(db, NAMES.COLLECTION.FILES)

    def add_face(self, api_key, face):
        self._faces_collection.insert_one({
            "face_name": face.name,
            "embeddings": [
                {
                    "array": face.embedding.array.tolist(),
                    "calculator_name": face.embedding.calculator_name
                }
            ],
            "raw_img_fs_id": self._faces_fs.put(face.raw_img.tobytes()),
            "face_img_fs_id": self._faces_fs.put(face.face_img.tobytes()),
            "api_key": api_key
        })

    def _get_faces_iterator(self, api_key):
        return self._faces_collection.find({"api_key": api_key})

    @staticmethod
    def _document_to_embedding(document):
        try:
            embedding_object = next((emb for emb in document['embeddings']
                                     if emb["calculator_name"] == EMBEDDING_CALCULATOR_MODEL_FILENAME))
        except StopIteration as e:
            raise FaceHasNoEmbeddingSavedError from e

        return Embedding(array=embedding_object['array'],
                         calculator_name=embedding_object['calculator_name'])

    def get_faces(self, api_key):
        def document_to_face(document):
            return Face(
                name=document['face_name'],
                embedding=self._document_to_embedding(document),
                raw_img=self._faces_fs.get(document['raw_img_fs_id']),
                face_img=self._faces_fs.get(document['face_img_fs_id'])
            )

        return [document_to_face(document) for document in self._get_faces_iterator(api_key)]

    def remove_face(self, api_key, face_name):
        self._faces_collection.delete_many({'face_name': face_name, 'api_key': api_key})

    def get_face_names(self, api_key):
        find_query = self._faces_collection.find(filter={"api_key": api_key}, projection={"face_name": 1})
        return find_query.distinct("face_name")

    def get_face_embeddings(self, api_key):
        def document_to_face_embedding(document):
            return FaceEmbedding(
                name=document['face_name'],
                embedding=self._document_to_embedding(document)
            )

        return [document_to_face_embedding(document) for document in self._get_faces_iterator(api_key)]

    def save_embedding_classifier(self, api_key, embedding_classifier):
        self._classifiers_collection.update({
            'name': embedding_classifier.name,
            'embedding_calculator_name': embedding_classifier.embedding_calculator_name,
            "api_key": api_key
        }, {
            'name': embedding_classifier.name,
            'embedding_calculator_name': embedding_classifier.embedding_calculator_name,
            "api_key": api_key,
            "class_2_face_name": {str(k): v for k, v in embedding_classifier.class_2_face_name.items()},
            "classifier_fs_id": self._classifiers_fs.put(serialize(embedding_classifier.model))
        }, upsert=True)

    def get_embedding_classifier(self, api_key, name, embedding_calculator_name):
        document = self._classifiers_collection.find_one({
            'name': name,
            'embedding_calculator_name': embedding_calculator_name,
            "api_key": api_key
        })
        if document is None:
            return None

        model = deserialize(self._classifiers_fs.get(document['classifier_fs_id']))
        class_2_face_name = {int(k): v for k, v in document['class_2_face_name']}
        embedding_calculator_name = document['embedding_calculator_name']
        return EmbeddingClassifier(name, model, class_2_face_name, embedding_calculator_name)

    def delete_embedding_classifiers(self, api_key):
        self._classifiers_collection.delete_many({'api_key': api_key})

    def get_api_keys(self):
        return self._faces_collection.find({}, {"projection": ["api_key"]}).distinct("api_key")

    def save_file(self, filename, bytes_data):
        result = self._files_fs.find_one({"filename": filename})
        if result:
            self._files_fs.delete(result['_id'])

        self._files_fs.put(bytes_data, filename=filename)

    def get_file(self, filename):
        result = self._files_fs.find_one({"filename": filename})
        return result.read() if result is not None else None
