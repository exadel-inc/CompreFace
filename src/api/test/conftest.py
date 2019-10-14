import pytest

import src


@pytest.fixture(scope='session')
def client():
    return src.app.test_client()
