import pytest

from src.storage.test.conftest import STORAGES


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__when_getting_embedding_calculator_model__then_returns_correct_model(storage):
    ...  # TODO EFRS-50


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_no_model__when_getting_embedding_calculator_model__then_raises_error(storage):
    ...  # TODO EFRS-50
