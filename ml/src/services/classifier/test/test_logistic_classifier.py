import joblib
import pytest

from src.services.classifier.logistic_classifier import LogisticClassifier
from src.services.utils.pyutils import get_current_dir

CURRENT_DIR = get_current_dir(__file__)


def load_embedding(name):
    return joblib.load(CURRENT_DIR / f'{name}.embedding.joblib')


@pytest.mark.integration
def test__given_2_embeddings__when_trained__then_correctly_classifies_3rd_embedding():
    embedding_a = load_embedding('01.A')
    embedding_b = load_embedding('02.A')
    new_embedding = load_embedding('07.B')
    embeddings = [embedding_a, embedding_b]
    names = ['Person A', 'Person B']
    emb_calc_version = 'cached_joblib'

    classifier = LogisticClassifier.train(embeddings, names, emb_calc_version)
    prediction = classifier.predict(new_embedding, emb_calc_version)

    assert prediction.face_name == 'Person A'
