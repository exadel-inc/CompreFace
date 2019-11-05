import logging
from typing import List

import numpy as np

from src.face_recognition.dto.bounding_box import BoundingBox
from src.face_recognition.dto.cropped_face import CroppedFace
from src.face_recognition.dto.face_prediction import FacePrediction
from src.face_recognition.embedding_calculator.calculator import calculate_embedding, CALCULATOR_VERSION
from src.face_recognition.embedding_classifier.train import CLASSIFIER_VERSION
from src.face_recognition.face_cropper.constants import FaceLimit
from src.face_recognition.face_cropper.cropper import crop_faces
from src.storage.dto.embedding_classifier import EmbeddingClassifier
from src.storage.storage import get_storage


def predict_from_embedding(classifier: EmbeddingClassifier, embedding, face_box: BoundingBox) -> FacePrediction:
    def _output_top_classes_to_log():
        for k, pred_class in enumerate(top_classes[:2], 1):
            logging.debug('Top prediction #%d: %s [class %d], probability: %.5f', k,
                          classifier.class_2_face_name[pred_class],
                          pred_class, probabilities[pred_class])

    # Get top classes
    probabilities = classifier.model.predict_proba([embedding])[0]
    top_classes = np.argsort(-probabilities)
    _output_top_classes_to_log()

    # Return data of the top class
    top_class = top_classes[0]
    return FacePrediction(face_name=classifier.class_2_face_name[top_class],
                          probability=probabilities[top_class], box=face_box)


def predict_from_image(img, limit: FaceLimit, api_key: str) -> List[FacePrediction]:
    classifier = get_storage(api_key).get_embedding_classifier(CLASSIFIER_VERSION, CALCULATOR_VERSION)

    def predict_from_cropped_face(face: CroppedFace):
        embedding = calculate_embedding(face.img)
        return predict_from_embedding(classifier, embedding, face.box)

    return [predict_from_cropped_face(cropped_face) for cropped_face in crop_faces(img, limit)]
