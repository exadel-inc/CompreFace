import pytest

from src.storage._mongo_storage import MongoStorage
from src.storage._mysql_storage import MySQLStorage

STORAGE_MOCKS = ['mongo_storage_mock', 'mysql_storage_mock']


@pytest.fixture
def mongo_storage_mock() -> MongoStorage:
    return MongoStorage()


@pytest.fixture
def mysql_storage_mock() -> MySQLStorage:
    return MySQLStorage()


@pytest.fixture
def storage_mock(request):
    return request.getfixturevalue(request.param)
