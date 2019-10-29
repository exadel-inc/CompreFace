import logging
from typing import Union, List

import numpy as np

from src.dto import BoundingBox
from src.dto.cropped_face import CroppedFace
from src.dto.face_prediction import FacePrediction
from src.dto.trained_model import TrainedModel
from src.face_recognition.embedding_calculator.calculator import calculate_embedding
from src.face_recognition.face_cropper.constants import FaceLimitConstant
from src.face_recognition.face_cropper.cropper import crop_faces
from src.storage.model_storage import get_model


def predict_from_embedding(embedding, face_box: BoundingBox, api_key: str) -> FacePrediction:
    model: TrainedModel = get_model(api_key)
    probabilities = model.classifier.predict_proba([embedding])[0]
    top_classes = np.argsort(-probabilities)

    for k, pred_class in enumerate(top_classes[:2], 1):
        logging.debug('Top prediction #%d: %s [class %d], probability: %.5f', k, model.class_2_face_name[pred_class],
                      pred_class, probabilities[pred_class])

    top_class = top_classes[0]
    probability = probabilities[top_class]
    face_name = model.class_2_face_name[top_class]
    return FacePrediction(face_name=face_name, probability=probability, box=face_box)


def predict_from_image(img, limit: Union[FaceLimitConstant, int], api_key: str) -> List[FacePrediction]:
    def predict_from_cropped_face(face: CroppedFace):
        embedding = calculate_embedding(face.img)
        return predict_from_embedding(embedding, face.box, api_key)

    return [predict_from_cropped_face(cropped_face) for cropped_face in crop_faces(img, limit)]
