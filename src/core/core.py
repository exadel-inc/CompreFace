import logging

import imageio

from src.core import _tf_helper, _classifier, calc_embedding
from src.core._image_helper import crop_faces


def init():
    _tf_helper.init()
    _classifier.initial_train()


def recognize_faces(limit, file, api_key):
    img = imageio.imread(file)
    face_img = crop_faces(img, limit)
    recognized_faces = []
    for face in range(0, len(face_img), 2):
        embedding = calc_embedding(face_img[face])
        face = _classifier.classify_many(embedding, api_key, face_img[face + 1].tolist())
        if face not in recognized_faces:
            recognized_faces.append(face)
    logging.debug("The faces that were found:", recognized_faces)
    return recognized_faces
