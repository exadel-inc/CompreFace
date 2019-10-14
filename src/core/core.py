import imageio

from src.core import _image_helper, _tf_helper, _classifier


def init():
    _tf_helper.init()
    _classifier.initial_train()


def recognize_faces(limit, file, api_key):
    img = imageio.imread(file)
    face_img = _image_helper.crop_faces(img, limit)
    recognized_faces = []
    for face in range(0, len(face_img), 2):
        embedding = _tf_helper.calc_embedding(face_img[face])
        face = _classifier.classify_many(embedding, api_key, face_img[face + 1].tolist())
        if face not in recognized_faces:
            recognized_faces.append(face)
    return recognized_faces
