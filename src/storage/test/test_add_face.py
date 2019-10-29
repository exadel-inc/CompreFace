import numpy as np
import pytest
from numpy import int8

from src.storage._mongo_storage import MongoStorage
from src.storage.test.conftest import STORAGES


@pytest.mark.xfail(reason="TODO EFRS-50")
@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__when_adding_face__then_adds_face(storage):
    data = dict(
        raw_img=np.zeros(shape=(15, 15, 3), dtype=int8),
        face_img=np.zeros(shape=(5, 5, 3), dtype=int8),
        embedding=np.zeros(shape=(16,), dtype=int8),
        face_name='Niels Bohr',
        api_key='api-key-001'
    )

    storage.add_face(**data)

    if isinstance(storage, MongoStorage):
        faces = list(storage._faces_collection.find())
        assert len(faces) == 1
        assert faces[0] == data  # TODO EFRS-50 Only certain faces[0] dict values should be compared
