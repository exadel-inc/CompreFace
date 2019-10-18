import pytest

from src.api.main import app


@pytest.fixture(scope='session')
def client():
    return app.test_client()
