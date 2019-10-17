import logging
from threading import Thread

import imageio
import numpy as np
from sklearn.linear_model import LogisticRegression

from src import pyutils
from src.database import get_storage
from src.faceclassifier.exceptions import ThereIsNoModelForAPIKeyError
from src.facecropper import crop_faces
from src.faceembedder import calc_embedding

models = {}

@pyutils.run_once
def train_all_models_once():
    api_keys = get_storage().get_api_keys()
    if not api_keys:
        print("[WARNING] Face classifier training hasn't been started, because no API Keys were found in storage.")
        exit(0)

    for api_key in api_keys:
        train(api_key)

def train_async(api_key):
    thread = Thread(target=train, daemon=False, args=[api_key])
    thread.start()


def train(api_key):
    logging.debug('Reading training data from mongo')
    values, labels, face_names = get_storage().get_train_data(api_key)
    if len(face_names) <= 1:
        logging.warning("Not enough training data, model hasn't been created")
        return
    logging.debug('Training classifier, api key: %s' % api_key)
    model_temp = LogisticRegression(C=100000, solver='lbfgs', multi_class='multinomial')
    model_temp.fit(values, labels)
    logging.debug('Training finished, api key: %s' % api_key)
    models[api_key] = {
        "model": model_temp,
        "face_names": face_names
    }


def classify_many(embedding, api_key, box):
    if api_key not in models:
        raise ThereIsNoModelForAPIKeyError("There is no model for api key %s." % api_key)
    model_data = models[api_key]
    predictions = model_data["model"].predict_proba([embedding])[0]
    logging.debug("predictions:")
    best_class_indices = np.argsort(-predictions)
    best_class_probability = predictions[best_class_indices[0]]
    logging.debug('Best guess: %s with probability %.5f' % (
        model_data["face_names"][best_class_indices[0]], best_class_probability))
    logging.debug('Second guess: %s with probability %.5f' % (
        model_data["face_names"][best_class_indices[1]], predictions[best_class_indices[1]]))
    return {
        "box_parameters": box,
        "prediction": model_data["face_names"][best_class_indices[0]],
        "probability": best_class_probability
    }


def recognize_faces(limit, file, api_key):
    img = imageio.imread(file)
    face_img = crop_faces(img, limit)
    recognized_faces = []
    for face in range(0, len(face_img), 2):
        embedding = calc_embedding(face_img[face])
        face = classify_many(embedding, api_key, face_img[face + 1].tolist())
        if face not in recognized_faces:
            recognized_faces.append(face)
    logging.debug("The faces that were found:", recognized_faces)
    return recognized_faces
