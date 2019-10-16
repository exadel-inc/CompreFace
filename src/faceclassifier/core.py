import logging

import imageio

from src.core._classifier import train_async as _train_async, classify_many
from src.facecropper import crop_faces
from src.faceembedder._tf_helper import calc_embedding as _calc_embedding



# def init():
#     _image_helper.init()
#     _tf_helper.init()
#     _classifier.initial_train()
def train_async(api_key):
    _train_async(api_key)


def calc_embedding(img):
    return _calc_embedding(img)


def recognize_faces(limit, file, api_key):
    img = imageio.imread(file)
    face_img = crop_faces(img, limit)
    recognized_faces = []
    for face in range(0, len(face_img), 2):
        embedding = _calc_embedding(face_img[face])
        face = classify_many(embedding, api_key, face_img[face + 1].tolist())
        if face not in recognized_faces:
            recognized_faces.append(face)
    logging.debug("The faces that were found:", recognized_faces)
    return recognized_faces
