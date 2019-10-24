import pytest

from src.storage.test.conftest import STORAGE_MOCKS


@pytest.mark.parametrize('storage_mock', STORAGE_MOCKS, indirect=True)
def test_(storage_mock):
    assert storage_mock in 'qm'
