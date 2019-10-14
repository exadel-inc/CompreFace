import pytest

import facerecognition


@pytest.fixture(scope='session')
def app():
    return facerecognition.app.test_client()
