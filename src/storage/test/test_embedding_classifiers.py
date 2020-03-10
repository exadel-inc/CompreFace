import pytest
from sklearn.linear_model import LogisticRegression

from src._pyutils.raises import raises
from src.storage.dto.embedding_classifier import EmbeddingClassifier
from src.storage.exceptions import NoTrainedEmbeddingClassifierFoundError
from src.storage.storage import Storage
from src.storage.test.conftest import STORAGES


def assert_classifiers_are_same(classifier1, classifier2):
    assert classifier1.class_2_face_name == classifier2.class_2_face_name
    assert classifier1.model.__repr__() == classifier2.model.__repr__()


CALC_NAME = 'calc1'
CLASSIFIER_NAME = 'LogisticRegression'


def get_classifier1():
    return EmbeddingClassifier(model=LogisticRegression(),
                               class_2_face_name={1: 'Erwin Schrodinger', 2: 'Ernest Rutherford'},
                               version=CLASSIFIER_NAME,
                               embedding_calculator_version=CALC_NAME)


def get_classifier2():
    return EmbeddingClassifier(model=LogisticRegression(),
                               class_2_face_name={1: 'Blaise Pascal', 2: 'Ludwig Boltzmann'},
                               version=CLASSIFIER_NAME,
                               embedding_calculator_version=CALC_NAME)


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_saved_classifier__when_getting_classifier__then_returns_the_saved_classifier(
        storage: Storage):
    classifier_in = get_classifier1()
    storage1 = storage.with_key(api_key='test-api-key')
    storage1.save_embedding_classifier(classifier_in)

    classifier_out = storage.with_key(api_key='test-api-key').get_embedding_classifier(CLASSIFIER_NAME, CALC_NAME)

    assert_classifiers_are_same(classifier_in, classifier_out)


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_overwritten_classifier__when_getting_classifier__then_returns_the_new_classifier(
        storage: Storage):
    classifier1, classifier2 = get_classifier1(), get_classifier2()
    storage1 = storage.with_key(api_key='test-api-key')
    storage1.save_embedding_classifier(classifier1)
    storage1.save_embedding_classifier(classifier2)

    classifier_out = storage.with_key(api_key='test-api-key').get_embedding_classifier(CLASSIFIER_NAME, CALC_NAME)

    assert_classifiers_are_same(classifier_out, classifier2)


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_saved_and_deleted_classifier__when_getting_classifier__then_raises_error(
        storage: Storage):
    classifier_in = get_classifier1()
    storage1 = storage.with_key(api_key='test-api-key')
    storage1.save_embedding_classifier(classifier_in)
    storage1.delete_embedding_classifier(classifier_in)

    def act():
        storage.with_key(api_key='test-api-key').get_embedding_classifier(CLASSIFIER_NAME, CALC_NAME)

    assert raises(NoTrainedEmbeddingClassifierFoundError, act)


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_saved_and_deleted_classifier__when_getting_classifier__then_raises_error(
        storage: Storage):
    classifier_in = get_classifier1()
    storage1 = storage.with_key(api_key='test-api-key')
    storage1.save_embedding_classifier(classifier_in)

    def act():
        storage.with_key(api_key='api-key-002').get_embedding_classifier(CLASSIFIER_NAME, CALC_NAME)

    assert raises(NoTrainedEmbeddingClassifierFoundError, act)
