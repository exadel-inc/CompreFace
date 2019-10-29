import pytest

from src.storage.test.conftest import STORAGES


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
@pytest.mark.parametrize('api_keys_in_db', [[], ['api-key-001', 'api-key-002']])
def test_integration__when_getting_api_keys__then_returns_same_api_keys(storage, api_keys_in_db):
    ...  # TODO EFRS-50
