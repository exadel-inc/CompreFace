import pytest

STORAGE_MOCKS = ['mongo_storage_mock', 'mysql_storage_mock']


@pytest.fixture
def mongo_storage_mock():
    return 'm'


@pytest.fixture
def mysql_storage_mock():
    return 'q'


@pytest.fixture
def storage_mock(request):
    return request.getfixturevalue(request.param)
