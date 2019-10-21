import pytest

from src.api.controller import app


@pytest.fixture(scope='session')
def client():
    return app.test_client()
