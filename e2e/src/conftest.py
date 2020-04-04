import logging
import time
from http import HTTPStatus

import pytest
import requests
from requests import ReadTimeout

from .constants import ENV


def after_previous_gen():
    order_no = 1
    while True:
        yield order_no
        order_no += 1


def _request(method, url, **kwargs):
    if 'timeout' not in kwargs or kwargs['timeout'] is None:
        kwargs['timeout'] = (ENV.CONNECT_TIMEOUT_S, ENV.READ_TIMEOUT_S)
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


def _wait_until_training_completes(ml_url, check_response=True):
    time.sleep(2)
    url = f"{ml_url}/retrain"
    timeout_s = ENV.TRAINING_TIMEOUT_S
    start_time = time.time()
    while True:
        res = GET(url, headers={'X-Api-Key': 'test-api-key'})
        if res.status_code != HTTPStatus.ACCEPTED:
            if check_response:
                assert res.status_code == HTTPStatus.OK
                assert res.json()['last_status'] == 'OK', res.content
            break
        if time.time() - start_time > timeout_s:
            raise Exception(f"Waiting to not get 202 from '{url}' has reached a timeout ({timeout_s}s)") from None
        time.sleep(1)


def _wait_for_available_service(ml_url):
    url = f"{ml_url}/status"
    timeout_s = ENV.AVAILABLE_SERVICE_TIMEOUT_S
    start_time = time.time()
    while True:
        try:
            res = GET(url)
        except (ConnectionError, ReadTimeout) as e:
            if time.time() - start_time > timeout_s:
                pytest.exit(f"Waiting to get 200 from '{url}' has reached a "
                            f"timeout ({timeout_s}s): {str(e)}", returncode=1)
            time.sleep(1)
            continue
        assert res.status_code == HTTPStatus.OK, res.content
        break
