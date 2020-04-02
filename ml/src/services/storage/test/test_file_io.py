from src.exceptions import NoFileFoundInDatabaseError
from src.services.storage.mongo_storage import MongoStorage
from src.services.utils.pytestutils import raises


def test__given_no_saved_file__when_getting_file__then_raises_error(storage: MongoStorage):
    pass

    def act():
        storage.get_file('filename.bin')

    assert raises(NoFileFoundInDatabaseError, act)


def test_given_saved_file__when_getting_file__then_returns_file(storage: MongoStorage):
    storage.save_file('filename.bin', b'hello')

    bytes_data = storage.get_file('filename.bin')

    assert bytes_data == b'hello'
