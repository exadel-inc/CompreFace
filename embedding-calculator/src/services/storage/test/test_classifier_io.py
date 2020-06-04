import pytest
from sklearn.linear_model import LogisticRegression

from src.exceptions import NoTrainedEmbeddingClassifierFoundError
from src.services.classifier.logistic_classifier import LogisticClassifier
from src.services.storage.mongo_storage import MongoStorage
from src.services.utils.pytestutils import raises


def assert_classifiers_are_same(classifier1, classifier2):
    assert classifier1.class_2_face_name == classifier2.class_2_face_name
    assert classifier1.model.__repr__() == classifier2.model.__repr__()


API_KEY = 'this-is-api-key'
EMB_VERSION = 'this-is-embedding-calculator-version'
VERSION = 'LogisticRegression'


@pytest.fixture
def classifier1():
    return LogisticClassifier(model=LogisticRegression(),
                              class_2_face_name={1: 'Erwin Schrodinger', 2: 'Ernest Rutherford'},
                              version=VERSION,
                              emb_calc_version=EMB_VERSION)


@pytest.fixture
def classifier2():
    return LogisticClassifier(model=LogisticRegression(),
                              class_2_face_name={1: 'Blaise Pascal', 2: 'Ludwig Boltzmann'},
                              version=VERSION,
                              emb_calc_version=EMB_VERSION)


def test__given_saved_classifier__when_getting_classifier__then_returns_the_saved_classifier(
        storage: MongoStorage, classifier1):
    storage.save_embedding_classifier(API_KEY, classifier1)

    classifier_out = storage.get_embedding_classifier(API_KEY, VERSION, EMB_VERSION)

    assert_classifiers_are_same(classifier1, classifier_out)


def test__given_overwritten_classifier__when_getting_classifier__then_returns_the_new_classifier(
        storage: MongoStorage, classifier1, classifier2):
    storage.save_embedding_classifier(API_KEY, classifier1)
    storage.save_embedding_classifier(API_KEY, classifier2)

    classifier_out = storage.get_embedding_classifier(API_KEY, VERSION, EMB_VERSION)

    assert_classifiers_are_same(classifier_out, classifier2)


def test__given_saved_and_deleted_classifier__when_getting_classifier__then_raises_error(
        storage: MongoStorage, classifier1):
    storage.save_embedding_classifier(API_KEY, classifier1)

    storage.delete_embedding_classifiers(API_KEY)

    def act():
        storage.get_embedding_classifier(API_KEY, VERSION, EMB_VERSION)

    assert raises(NoTrainedEmbeddingClassifierFoundError, act)


def test__given_different_api_key__when_getting_classifier__then_raises_error(
        storage: MongoStorage, classifier1):
    storage.save_embedding_classifier(API_KEY, classifier1)

    def act():
        storage.get_embedding_classifier('api-key-002', VERSION, EMB_VERSION)

    assert raises(NoTrainedEmbeddingClassifierFoundError, act)
