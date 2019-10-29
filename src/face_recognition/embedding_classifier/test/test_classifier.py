import os
from pathlib import Path

import joblib
import pytest

from src.dto import BoundingBox
from src.dto.trained_model import TrainedModel
from src.face_recognition.embedding_classifier.predict import predict_from_embedding
from src.face_recognition.embedding_classifier.train import get_trained_classifier

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))


@pytest.mark.integration
def test_integration__given_2_faces_in_db__when_asked_to_recognize_known_face__then_recognizes_correct_face(mocker):
    person_a_emb_1 = joblib.load(CURRENT_DIR / 'files' / 'personA-img1.embedding.joblib')
    person_a_emb_2 = joblib.load(CURRENT_DIR / 'files' / 'personA-img2.embedding.joblib')
    person_b_emb_1 = joblib.load(CURRENT_DIR / 'files' / 'personB-img1.embedding.joblib')

    classifier = get_trained_classifier(values=[person_a_emb_1, person_b_emb_1], labels=[0, 1])
    trained_model = TrainedModel(classifier=classifier,
                                 class_2_face_name={0: 'Person A', 1: 'Person B'})
    face_prediction = predict_from_embedding(trained_model, person_a_emb_2, BoundingBox(0, 0, 0, 0))

    assert face_prediction.face_name == 'Person A'
