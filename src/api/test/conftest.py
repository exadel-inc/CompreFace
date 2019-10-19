import pytest

from src.api.app import app


@pytest.fixture(scope='session')
def client():
    return app.test_client()
