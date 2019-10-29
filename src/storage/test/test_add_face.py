import pytest
from mongomock.gridfs import enable_gridfs_integration
from numpy.core.multiarray import ndarray

from src.storage._mongo_storage import MongoStorage


@pytest.fixture
def gridfs_support():
    enable_gridfs_integration()


@pytest.mark.usefixtures('gridfs_support')
@pytest.fixture
def storage(mongodb):
    return MongoStorage(mongo_client=mongodb)


@pytest.mark.usefixtures('gridfs_support')
def test__when_adding_face__face_is_added(storage):
    pass

    storage.add_face(raw_img=ndarray(shape=(15, 15, 3)),
                     face_img=ndarray(shape=(5, 5, 3)),
                     embedding=ndarray(shape=(16,)),
                     face_name='Niels Bohr',
                     api_key='api-key-001')

    client = mongomock.MongoClient()
    db = client.db
    runs = db.runs
    metrics = db.metrics
    fs = gridfs.GridFS(db)

    pass
