import logging
import time
from http import HTTPStatus

import pytest
import requests
from requests import ConnectionError
from requests import ReadTimeout

from .constants import TIMEOUT_MULTIPLIER

CONNECT_TIMEOUT_S = TIMEOUT_MULTIPLIER * 5
READ_TIMEOUT_S = TIMEOUT_MULTIPLIER * 30
BBOX_ALLOWED_PX_DIFFERENCE = 10
EMB_SIMILARITY_THRESHOLD = 0.01
AVAILABLE_SERVICE_TIMEOUT_S = TIMEOUT_MULTIPLIER * 8
TRAINING_TIMEOUT_S = TIMEOUT_MULTIPLIER * 30


def pytest_addoption(parser):
    # Run E2E against this host, default value: http://localhost:3000
    parser.addoption('--host', action='store', dest='host', default='http://localhost:3000')


@pytest.fixture
def host(request):
    return request.config.getoption('host')


def after_previous_gen():
    order_no = 1
    while True:
        yield order_no
        order_no += 1


def _request(method, url, **kwargs):
    if 'timeout' not in kwargs or kwargs['timeout'] is None:
        kwargs['timeout'] = (CONNECT_TIMEOUT_S, READ_TIMEOUT_S)
    try:
        return requests.request(method, url, **kwargs)
    except requests.exceptions.ConnectionError as e:
        logging.error(str(e))
        raise ConnectionError(e) from None


# noinspection PyPep8Naming
def GET(url, **kwargs):
    return _request('get', url, **kwargs)


# noinspection PyPep8Naming
def POST(url, **kwargs):
    return _request('post', url, **kwargs)


# noinspection PyPep8Naming
def DELETE(url, **kwargs):
    return _request('delete', url, **kwargs)


def _embeddings_are_the_same(embedding1, embedding2):
    for i in range(len(embedding1)):
        if (embedding1[i] - embedding2[i]) / embedding2[i] > EMB_SIMILARITY_THRESHOLD:
            return False
    return True


def _boxes_are_the_same(box1, box2):
    def value_is_the_same(key):
        return abs(box2[key] - box1[key]) <= BBOX_ALLOWED_PX_DIFFERENCE

    return (value_is_the_same('x_max')
            and value_is_the_same('x_min')
            and value_is_the_same('y_max')
            and value_is_the_same('y_min'))


def _wait_for_available_service(host):
    url = f"{host}/status"
    timeout_s = AVAILABLE_SERVICE_TIMEOUT_S
    start_time = time.time()
    while True:
        try:
            res = GET(url, headers={'X-Api-Key': 'test-api-key'})
        except (ConnectionError, ReadTimeout) as e:
            if time.time() - start_time > timeout_s:
                raise Exception(f"Waiting to get 200 from '{url}' has reached a "
                                f"timeout ({timeout_s}s): {str(e)}") from None
            time.sleep(1)
            continue
        assert res.status_code == HTTPStatus.OK, res.content
        break
