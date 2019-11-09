import numpy as np
import pytest

from src.face_recognition.dto.embedding import Embedding
from src.storage.dto.face import Face
from src.storage.storage import Storage
from src.storage.test.conftest import STORAGES


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_no_saved_faces__when_getting_api_keys__then_returns_empty_array(storage: Storage):
    pass

    api_keys = storage.get_api_keys()

    assert len(api_keys) == 0


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_saved_faces__when_getting_api_keys__then_returns_the_api_keys(storage: Storage):
    face = Face(name='Robert Oppenheimer',
                raw_img=np.zeros(shape=(15, 15, 3)),
                face_img=np.zeros(shape=(5, 5, 3)),
                embedding=Embedding(array=np.zeros(shape=(16,)), calculator_version='A'))
    face2 = Face(name='Albert Einstein',
                 raw_img=np.zeros(shape=(15, 15, 3)),
                 face_img=np.zeros(shape=(5, 5, 3)),
                 embedding=Embedding(array=np.zeros(shape=(16,)), calculator_version='A'))
    storage.with_key('test-api-key').add_face(face)
    storage.with_key('test-api-key').add_face(face2)
    storage.with_key('api-key-002').add_face(face)

    api_keys = storage.get_api_keys()

    assert set(api_keys) == {'test-api-key', 'api-key-002'}
