from src.exceptions import NoFileFoundInDatabaseError
from src.services.storage.mongo_storage import MongoStorage
from src.services.utils.pytestutils import raises

FILENAME = 'filename.bin'

def test__given_no_saved_file__when_getting_file__then_raises_error(storage: MongoStorage):
    pass  # NOSONAR

    def act():
        storage.get_file(FILENAME)

    assert raises(NoFileFoundInDatabaseError, act)


def test__given_saved_file__when_getting_file__then_returns_file(storage: MongoStorage):
    storage.save_file(FILENAME, b'hello')

    bytes_data = storage.get_file(FILENAME)

    assert bytes_data == b'hello'
