import os

import gridfs
from pymongo import MongoClient

from src.storage.storage_base import StorageBase

CURRENT_MODEL_NAME = "20170512-110547.pb"
MODELS_COLLECTION_NAME = "models"
FACES_COLLECTION_NAME = "faces"
DATABASE_NAME = "recognition"


class MongoStorage(StorageBase):
    def __init__(self):
        self._mongo_client = MongoClient(host=os.environ.get('MONGO_HOST', 'mongo'),
                                         port=int(os.environ.get('MONGO_PORT', '27017')))
        db = self._mongo_client[DATABASE_NAME]
        self._db = db
        self._faces_fs = gridfs.GridFS(db, FACES_COLLECTION_NAME)
        self._models_fs = gridfs.GridFS(db, MODELS_COLLECTION_NAME)
        self._faces_collection = db[FACES_COLLECTION_NAME]

    def get_embedding_calculator_model(self):
        model = self._models_fs.find_one({"filename": CURRENT_MODEL_NAME})
        if model is None:
            raise RuntimeError("Can't find a model file %s." % CURRENT_MODEL_NAME)
        return model.read()

    def add_face(self, raw_img, face_img, embedding, face_name, api_key):
        raw_img_id = self._faces_fs.put(raw_img.tobytes())
        face_img_id = self._faces_fs.put(face_img.tobytes())
        face_object = {
            "face_name": face_name,
            "embeddings": [
                {
                    "embedding": embedding.tolist(),
                    "model_name": CURRENT_MODEL_NAME
                }
            ],
            "raw_img_id": raw_img_id,
            "face_img_id": face_img_id,
            "api_key": api_key
        }
        self._faces_collection.insert_one(face_object)

    def get_classifier_training_data(self, api_key):
        values = []
        labels = []
        face_names = {}
        curr_face_encoding = -1
        conditions = {"embeddings.model_name": CURRENT_MODEL_NAME, "api_key": api_key}
        for face in self._faces_collection.find(conditions).sort("face_name"):
            if face['face_name'] not in face_names:
                curr_face_encoding += 1
                face_names[face['face_name']] = curr_face_encoding
            labels.append(curr_face_encoding)
            embeddings = face['embeddings']
            found = next((item for item in embeddings if item["model_name"] == CURRENT_MODEL_NAME))
            values.append(found['embedding'])
        return values, labels, {v: k for k, v in face_names.items()}

    def get_api_keys(self):
        return self._faces_collection.find({}, {"projection": ["api_key"]}).distinct("api_key")

    def get_all_face_names(self, api_key):
        return self._faces_collection.find({"embeddings.model_name": CURRENT_MODEL_NAME, "api_key": api_key},
                                           {"face_name": 1}).distinct("face_name")

    def remove_face(self, api_key, face_name):
        return self._faces_collection.delete_many(
            {"embeddings.model_name": CURRENT_MODEL_NAME, "api_key": api_key, 'face_name': face_name})
