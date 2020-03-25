import logging
import time
from http import HTTPStatus
from typing import Union

import pytest
import requests
from requests import ReadTimeout

from .constants import TIMEOUT_MULTIPLIER

CONNECT_TIMEOUT_S = TIMEOUT_MULTIPLIER * 5
READ_TIMEOUT_S = TIMEOUT_MULTIPLIER * 30
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


def _wait_until_training_completes(host, expected_code: Union[HTTPStatus, None] = HTTPStatus.OK):
    time.sleep(2)
    url = f"{host}/retrain"
    timeout_s = TRAINING_TIMEOUT_S
    start_time = time.time()
    while True:
        res = GET(url, headers={'X-Api-Key': 'test-api-key'})
        if res.status_code != HTTPStatus.ACCEPTED:
            if expected_code:
                assert res.status_code == expected_code
            break
        if time.time() - start_time > timeout_s:
            raise Exception(f"Waiting to not get 202 from '{url}' has reached a timeout ({timeout_s}s)") from None
        time.sleep(1)


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
