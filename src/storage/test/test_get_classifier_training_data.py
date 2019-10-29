import pytest

from src.storage.test.conftest import STORAGES


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__when_getting_classifier_data__then_returns_correct_data(storage):
    ...  # TODO EFRS-50
