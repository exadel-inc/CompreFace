import pytest


@pytest.fixture
def app():
    from src.app import create_app
    return create_app()
