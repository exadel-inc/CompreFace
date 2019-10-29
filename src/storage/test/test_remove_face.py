import pytest

from src.storage.test.conftest import STORAGES


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_no_faces__when_removing_face__then_continues_without_error(storage):
    ...  # TODO EFRS-50


@pytest.mark.integration
@pytest.mark.parametrize('storage', STORAGES, indirect=True)
def test_integration__given_existing_face__when_removing_the_face__then_removes_it(storage):
    ...  # TODO EFRS-50
