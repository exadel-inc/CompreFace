import numpy as np
import pytest

from src.exceptions import FaceHasNoEmbeddingCalculatedError
from src.services.storage.face import Face
from src.services.storage.mongo_storage import MongoStorage
from src.services.utils.pytestutils import raises

API_KEY = 'this-is-api-key'
EMB_VERSION = 'this-is-embedding-calculator-version'


@pytest.fixture
def face1():
    return Face(name='Robert Oppenheimer',
                raw_img=np.zeros(shape=(2, 2, 3)),
                face_img=np.zeros(shape=(1, 1, 3)),
                embedding=np.zeros(shape=(16,)))


@pytest.fixture
def face2():
    return Face(name='Nikola Tesla',
                raw_img=np.ones(shape=(2, 2, 3)),
                face_img=np.ones(shape=(1, 1, 3)),
                embedding=np.ones(shape=(16,)))


def test__given_saved_faces__when_getting_faces__then_returns_the_faces(
        storage: MongoStorage, face1, face2):
    storage.add_face(API_KEY, face1, EMB_VERSION)
    storage.add_face(API_KEY, face2, EMB_VERSION)

    returned_faces = storage.get_faces(API_KEY, EMB_VERSION)

    assert set(f.name for f in returned_faces) == {face1.name, face2.name}
    assert (set(f.raw_img.tostring() for f in returned_faces) == {face1.raw_img.tostring(),
                                                                  face2.raw_img.tostring()})
    assert (set(f.face_img.tostring() for f in returned_faces) == {face1.face_img.tostring(),
                                                                   face2.face_img.tostring()})
    assert (set(f.embedding.tostring() for f in returned_faces) == {face1.embedding.tostring(),
                                                                    face2.embedding.tostring()})


def test__given_saved_faces__when_getting_names__then_returns_the_names(
        storage: MongoStorage, face1, face2):
    storage.add_face(API_KEY, face1, EMB_VERSION)
    storage.add_face(API_KEY, face2, EMB_VERSION)

    returned_names = storage.get_face_names(API_KEY)

    assert set(returned_names) == {'Robert Oppenheimer', 'Nikola Tesla'}


def test__given_saved_faces__when_getting_face_embeddings__then_returns_the_face_embeddings(
        storage: MongoStorage, face1, face2):
    storage.add_face(API_KEY, face1, EMB_VERSION)
    storage.add_face(API_KEY, face2, EMB_VERSION)

    returned_face_embeddings = storage.get_face_embeddings(API_KEY, EMB_VERSION)

    assert set(e.name for e in returned_face_embeddings) == {'Robert Oppenheimer', 'Nikola Tesla'}
    assert set(e.embedding.tostring() for e in returned_face_embeddings) == {np.zeros(shape=(16,)).tostring(),
                                                                             np.ones(shape=(16,)).tostring()}


def test__given_saved_faces__when_deleting_faces__then_faces_are_removed(
        storage: MongoStorage, face1):
    storage.add_face(API_KEY, face1, EMB_VERSION)

    storage.remove_face(API_KEY, 'Robert Oppenheimer')

    returned_faces = storage.get_faces(API_KEY, EMB_VERSION)
    assert len(returned_faces) == 0


def test__given_saved__when_deleting_face_twice__then_does_not_raise_error(
        storage: MongoStorage, face1):
    storage.add_face(API_KEY, face1, EMB_VERSION)

    storage.remove_face(API_KEY, 'Robert Oppenheimer')
    storage.remove_face(API_KEY, 'Robert Oppenheimer')

    returned_faces = storage.get_faces(API_KEY, EMB_VERSION)
    assert len(returned_faces) == 0


def test__given_saved_faces__when_getting_faces_with_different_api_key__then_returns_empty_array(
        storage: MongoStorage, face1):
    storage.add_face(API_KEY, face1, EMB_VERSION)

    returned_faces = storage.get_faces('different-api-key', EMB_VERSION)
    assert len(returned_faces) == 0


def test__given_saved_faces__when_getting_faces_with_different_calculator_version__then_raises_error(
        storage: MongoStorage, face1):
    storage.add_face(API_KEY, face1, EMB_VERSION)

    def act():
        storage.get_faces(API_KEY, 'different-calculator-version')

    assert raises(FaceHasNoEmbeddingCalculatedError, act)
