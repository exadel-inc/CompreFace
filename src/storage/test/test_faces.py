import numpy as np
import pytest

from src.scan_faces.dto.embedding import Embedding
from src.storage.dto.face import Face, FaceEmbedding
from src.storage.storage import Storage
from src.storage.test.conftest import STORAGES


def get_face1():
    return Face(name='Robert Oppenheimer',
                raw_img=np.zeros(shape=(15, 15, 3)),
                face_img=np.zeros(shape=(5, 5, 3)),
                embedding=Embedding(array=np.zeros(shape=(16,)), calculator_version='v1'))


def get_face2():
    return Face(name='Nikola Tesla',
                raw_img=np.ones(shape=(15, 15, 3)),
                face_img=np.ones(shape=(5, 5, 3)),
                embedding=Embedding(array=np.zeros(shape=(16,)), calculator_version='v1'))


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_saved_faces__when_getting_faces__then_returns_the_faces(storage: Storage):
    face1, face2 = get_face1(), get_face2()
    storage1 = storage.with_key('test-api-key')
    storage1.add_face(face1)
    storage1.add_face(face2)

    returned_faces = storage.with_key('test-api-key').get_faces(calculator_version='v1')

    assert set(returned_faces) == {face1, face2}


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_saved_faces__when_getting_names__then_returns_the_names(storage: Storage):
    face1, face2 = get_face1(), get_face2()
    storage1 = storage.with_key('test-api-key')
    storage1.add_face(face1)
    storage1.add_face(face2)

    returned_names = storage.with_key('test-api-key').get_face_names()

    assert set(returned_names) == {'Robert Oppenheimer', 'Nikola Tesla'}


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_saved_faces__when_getting_face_embeddings__then_returns_the_face_embeddings(
        storage: Storage):
    storage1 = storage.with_key('test-api-key')
    storage1.add_face(Face(name='Robert Oppenheimer',
                           raw_img=np.zeros(shape=(15, 15, 3)),
                           face_img=np.zeros(shape=(5, 5, 3)),
                           embedding=Embedding(array=np.zeros(shape=(16,)), calculator_version='v1')))
    storage1.add_face(Face(name='Nikola Tesla',
                           raw_img=np.ones(shape=(15, 15, 3)),
                           face_img=np.ones(shape=(5, 5, 3)),
                           embedding=Embedding(array=np.zeros(shape=(16,)), calculator_version='v1')))

    returned_face_embeddings = storage.with_key('test-api-key').get_face_embeddings(calculator_version='v1')

    assert set(returned_face_embeddings) == {
        FaceEmbedding(name='Robert Oppenheimer',
                      embedding=Embedding(array=np.zeros(shape=(16,)), calculator_version='v1')),
        FaceEmbedding(name='Nikola Tesla',
                      embedding=Embedding(array=np.zeros(shape=(16,)), calculator_version='v1'))
    }


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_saved_and_deleted_faces__when_getting_faces__then_returns_empty_array(storage: Storage):
    storage1 = storage.with_key('test-api-key')
    storage1.add_face(Face(name='Robert Oppenheimer',
                           raw_img=np.zeros(shape=(15, 15, 3)),
                           face_img=np.zeros(shape=(5, 5, 3)),
                           embedding=Embedding(array=np.zeros(shape=(16,)), calculator_version='v1')))
    storage1.remove_face('Robert Oppenheimer')

    returned_faces = storage.with_key('test-api-key').get_faces(calculator_version='v1')

    assert len(returned_faces) == 0


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_saved__when_deleting_face_twice__then_does_not_raise_error(storage: Storage):
    storage1 = storage.with_key('test-api-key')
    storage1.add_face(Face(name='Robert Oppenheimer',
                           raw_img=np.zeros(shape=(15, 15, 3)),
                           face_img=np.zeros(shape=(5, 5, 3)),
                           embedding=Embedding(array=np.zeros(shape=(16,)), calculator_version='v1')))
    storage1.remove_face('Robert Oppenheimer')
    storage1.remove_face('Robert Oppenheimer')

    returned_faces = storage.with_key('test-api-key').get_faces(calculator_version='v1')

    assert len(returned_faces) == 0


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_saved_faces__when_getting_faces_with_different_api_key__then_returns_empty_array(
        storage: Storage):
    storage.with_key('test-api-key').add_face(Face(name='Robert Oppenheimer',
                                                   raw_img=np.zeros(shape=(15, 15, 3)),
                                                   face_img=np.zeros(shape=(5, 5, 3)),
                                                   embedding=Embedding(array=np.zeros(shape=(16,)),
                                                                       calculator_version='v1')))

    returned_faces = storage.with_key('api-key-002').get_faces(calculator_version='v1')

    assert len(returned_faces) == 0
