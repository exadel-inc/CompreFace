import mongomock
import pytest
from mongomock.gridfs import enable_gridfs_integration

from src.storage._database_wrapper.database_mongo import DatabaseMongo
from src.storage.storage import Storage


@pytest.mark.usefixtures()
@pytest.fixture
def with_mongo_database(mocker):
    enable_gridfs_integration()
    mocker.patch('src.storage._database_wrapper.database_mongo.MongoClient', mongomock.MongoClient)
    return Storage(DatabaseMongo())


STORAGES = ['with_mongo_database']


@pytest.fixture
def storage(request):
    return request.getfixturevalue(request.param)
