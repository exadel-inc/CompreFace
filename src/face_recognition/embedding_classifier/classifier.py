import logging
from threading import Thread
from typing import List, Union

import numpy as np
from sklearn.linear_model import LogisticRegression

from src.dto import BoundingBox
from src.dto.cropped_face import CroppedFace
from src.dto.face_prediction import FacePrediction
from src.face_recognition.embedding_calculator.calculator import calculate_embedding
from src.face_recognition.face_cropper.constants import FaceLimitConstant
from src.face_recognition.face_cropper.cropper import crop_faces
from src.storage.storage_factory import get_storage

models = {}


def train(api_key):
    logging.debug('Reading training data from mongo')
    values, labels, face_names = get_storage().get_classifier_training_data(api_key)
    if len(face_names) <= 1:
        logging.warning("Not enough training data, model hasn't been created")
        if api_key in models:
            del models[api_key]
        return
    logging.debug('Training classifier, api key: %s' % api_key)
    model_temp = LogisticRegression(C=100000, solver='lbfgs', multi_class='multinomial')
    model_temp.fit(values, labels)
    logging.debug('Training finished, api key: %s' % api_key)
    models[api_key] = {
        "model": model_temp,
        "face_names": face_names
    }


def train_async(api_key):
    thread = Thread(target=train, daemon=False, args=[api_key])
    thread.start()
    return thread


def train_all_models():
    api_keys = get_storage().get_api_keys()
    if not api_keys:
        logging.warning("Face classifier training for all models hasn't been started, "
                        "because no API Keys were found in storage.")
        return

    for api_key in api_keys:
        train(api_key)


def get_face_predictions(img, limit: Union[FaceLimitConstant, int], api_key) -> List[FacePrediction]:
    def _classify_many(embedding, api_key, box: BoundingBox):
        assert api_key in models, 'No model is yet trained for this api key'
        model_data = models[api_key]
        predictions = model_data["model"].predict_proba([embedding])[0]
        logging.debug("predictions:")
        best_class_indices = np.argsort(-predictions)
        best_class_probability = predictions[best_class_indices[0]]
        logging.debug('Best guess: %s with probability %.5f', model_data["face_names"][best_class_indices[0]],
                      best_class_probability)
        logging.debug('Second guess: %s with probability %.5f', model_data["face_names"][best_class_indices[1]],
                      predictions[best_class_indices[1]])
        prediction = model_data["face_names"][best_class_indices[0]]
        return FacePrediction(box=box, prediction=prediction, probability=best_class_probability)

    faces: List[CroppedFace] = crop_faces(img, limit)
    recognized_faces = []
    for face in faces:
        embedding = calculate_embedding(face.img)
        recognized_face = _classify_many(embedding, api_key, face.box)
        recognized_faces.append(recognized_face)
    return recognized_faces
