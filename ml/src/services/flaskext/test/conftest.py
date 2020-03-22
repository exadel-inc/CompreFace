import pytest

from src.app import create_app


@pytest.fixture
def app():
    return create_app()
