import pytest

from src.storage.test.conftest import STORAGES


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
@pytest.mark.parametrize('db_setup, returned_value', [
    ({},
     []),
    ({'api-key-001': ['Stephen Hawking', 'Richard Feynman'],
      'api-key-002': ['Max Planck']},
     ['Stephen Hawking', 'Richard Feynman'])
])
def test_integration__when_getting_face_names__then_returns_face_names(storage, db_setup, returned_value):
    ...  # TODO EFRS-50
