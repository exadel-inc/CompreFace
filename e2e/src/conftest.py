import logging

import pytest
import requests

CONNECT_TIMEOUT_S = 5
READ_TIMEOUT_S = 30


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
