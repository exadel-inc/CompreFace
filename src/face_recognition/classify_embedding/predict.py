from typing import List

import numpy as np

from src.face_recognition.calc_embedding.calculator import CALCULATOR_VERSION, calculate_embeddings
from src.face_recognition.classify_embedding.train import CLASSIFIER_VERSION
from src.face_recognition.crop_faces.constants import FaceLimit
from src.face_recognition.crop_faces.crop_faces import crop_faces
from src.face_recognition.dto.bounding_box import BoundingBox
from src.face_recognition.dto.embedding import Embedding
from src.face_recognition.dto.face_prediction import FacePrediction
from src.storage.dto.embedding_classifier import EmbeddingClassifier
from src.storage.storage import get_storage


def predict_from_embedding(classifier: EmbeddingClassifier, embedding: Embedding,
                           face_box: BoundingBox, is_face_prob) -> FacePrediction:
    probabilities = classifier.model.predict_proba([embedding.array])[0]
    top_class = np.argsort(-probabilities)[0]
    return FacePrediction(face_name=classifier.class_2_face_name[top_class],
                          probability=probabilities[top_class], box=face_box, is_face_prob=is_face_prob)


def predict_from_image(img, limit: FaceLimit, detection_threshold_c, api_key: str) -> List[FacePrediction]:
    classifier = get_storage(api_key).get_embedding_classifier(CLASSIFIER_VERSION, CALCULATOR_VERSION)

    cropped_faces = crop_faces(img, limit, detection_threshold_c)
    embeddings = calculate_embeddings(images=[face.img for face in cropped_faces])
    return [predict_from_embedding(classifier, embedding, face.box, face.is_face_prob)
            for face, embedding in zip(cropped_faces, embeddings)]
