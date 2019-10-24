import pytest

from src.storage._storage_base import StorageBase
from src.storage.test.conftest import STORAGE_MOCKS


@pytest.mark.parametrize('storage_mock', STORAGE_MOCKS, indirect=True)
def test__given_no_keys_in_db__when_invoking_get_api_keys__returns_empty_list(storage_mock: StorageBase):
    pass

    ret = storage_mock.get_api_keys()

    assert ret == []
