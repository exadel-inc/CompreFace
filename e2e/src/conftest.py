import pytest
import requests


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


def GET(url, *args, **kwargs):
    try:
        return requests.get(url, *args, **kwargs)
    except requests.exceptions.ConnectionError as e:
        raise ConnectionError(f"Could not reach '{url}', {str(e)}") from None


def POST(url, *args, **kwargs):
    try:
        return requests.post(url, *args, **kwargs)
    except requests.exceptions.ConnectionError as e:
        raise ConnectionError(f"Could not reach '{url}', {str(e)}") from None


def DELETE(url, **kwargs):
    try:
        return requests.delete(url, **kwargs)
    except requests.exceptions.ConnectionError as e:
        raise ConnectionError(f"Could not reach '{url}', {str(e)}") from None
