import pytest

from src.pyutils.raises import raises
from src.storage.exceptions import NoFileFoundInDatabaseError
from src.storage.storage import Storage
from src.storage.test.conftest import STORAGES


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_no_saved_file__when_getting_file__then_raises_error(storage: Storage):
    pass

    def act():
        storage.get_file('filename.bin')

    assert raises(NoFileFoundInDatabaseError, act)


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_saved_file__when_getting_file__then_returns_file(storage: Storage):
    storage.save_file('filename.bin', b'hello')

    bytes_data = storage.get_file('filename.bin')

    assert bytes_data == b'hello'
