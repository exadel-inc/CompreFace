import mongomock
import pytest
from mongomock.gridfs import enable_gridfs_integration

from src.services.storage.mongo_storage import MongoStorage


@pytest.fixture
def storage(mocker):
    enable_gridfs_integration()
    mocker.patch('src.services.storage.mongo_storage.MongoClient', mongomock.MongoClient)
    return MongoStorage(host='test', port=1000)
