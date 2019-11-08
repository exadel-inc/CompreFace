import logging
from typing import List

import numpy as np

from src.dto import BoundingBox
from src.dto.face_prediction import FacePrediction
from src.dto.trained_model import TrainedModel
from src.face_recognition.embedding_calculator.calculator import calculate_embedding
from src.face_recognition.face_cropper.constants import FaceLimit
from src.face_recognition.face_cropper.cropper import crop_faces
from src.storage.trained_model_storage import get_trained_model


def predict_from_embedding(model: TrainedModel, embedding, face_box: BoundingBox) -> FacePrediction:
    probabilities = model.classifier.predict_proba([embedding])[0]
    top_classes = np.argsort(-probabilities)

    for k, pred_class in enumerate(top_classes[:2], 1):
        logging.debug('Top prediction #%d: %s [class %d], probability: %.5f', k, model.class_2_face_name[pred_class],
                      pred_class, probabilities[pred_class])

    top_class = top_classes[0]
    probability = probabilities[top_class]
    face_name = model.class_2_face_name[top_class]
    return FacePrediction(face_name=face_name, probability=probability, box=face_box)


def predict_from_image(img, limit: FaceLimit, api_key: str) -> List[FacePrediction]:
    model = get_trained_model(api_key)
    faces = crop_faces(img, limit)
    return [predict_from_embedding(model, calculate_embedding(face.img), face.box) for face in faces]
