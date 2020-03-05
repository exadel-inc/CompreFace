import os
from pathlib import Path

import joblib
import pytest

from src.classifier.predict import predict_from_embedding
from src.classifier.train import get_trained_model
from src.facescanner.dto.bounding_box import BoundingBox
from src.facescanner.dto.embedding import Embedding
from src.storage.dto.embedding_classifier import EmbeddingClassifier

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))


def _load_embedding(name):
    return Embedding(array=joblib.load(CURRENT_DIR / 'files' / f'{name}.joblib'),
                     calculator_version='test-version')


@pytest.mark.integration
def test_integration__given_2_faces_in_db__when_asked_to_recognize_known_face__then_recognizes_correct_face():
    person_a_emb_1 = _load_embedding('personA-img1.embedding')
    person_a_emb_2 = _load_embedding('personA-img2.embedding')
    person_b_emb_1 = _load_embedding('personB-img1.embedding')
    embedding_arrays = [person_a_emb_1.array, person_b_emb_1.array]

    version, model = get_trained_model(values=embedding_arrays, labels=[0, 1])
    embedding_classifier = EmbeddingClassifier(model=model, version=version,
                                               class_2_face_name={0: 'Person A', 1: 'Person B'},
                                               embedding_calculator_version='test-version')
    face_prediction = predict_from_embedding(embedding_classifier, person_a_emb_2, BoundingBox(0, 0, 0, 0, 0.9))

    assert face_prediction.face_name == 'Person A'
