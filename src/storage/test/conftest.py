import mongomock
import pytest
from mongomock.gridfs import enable_gridfs_integration

from src.storage._mongo_storage import MongoStorage


@pytest.mark.usefixtures()
@pytest.fixture
def mongo(mocker):
    enable_gridfs_integration()
    mocker.patch('src.storage._mongo_storage.MongoClient', mongomock.MongoClient)
    return MongoStorage()


STORAGES = ['mongo']


@pytest.fixture
def storage(request):
    return request.getfixturevalue(request.param)
