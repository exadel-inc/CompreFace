import logging
import time
from http import HTTPStatus

import pytest
import requests

from src import constants
from src.constants import ENV_E2E

logger = logging.getLogger(__name__)


def _request(method, endpoint, **kwargs):
    if 'timeout' not in kwargs or kwargs['timeout'] is None:
        kwargs['timeout'] = (constants.CONNECT_TIMEOUT_S, constants.READ_TIMEOUT_S)
    try:
        return requests.request(method, f"{ENV_E2E.ML_URL}{endpoint}", **kwargs)
    except requests.RequestException as e:
        logger.error(str(e))
        raise ConnectionError(e) from None


def ml_get(endpoint, **kwargs):
    return _request('get', endpoint, **kwargs)


def ml_post(endpoint, **kwargs):
    return _request('post', endpoint, **kwargs)


def ml_delete(endpoint, **kwargs):
    return _request('delete', endpoint, **kwargs)


def ml_wait_until_training_is_completed(api_key, check_response=True):
    time.sleep(2)
    endpoint = "/retrain"
    timeout_s = constants.TRAINING_TIMEOUT_S
    start_time = time.time()
    while True:
        res = ml_get(endpoint, headers={'X-Api-Key': api_key})
        if res.status_code != HTTPStatus.ACCEPTED:
            if check_response:
                assert res.status_code == HTTPStatus.OK
                assert res.json()['last_training_status'] == 'OK', res.content
            break
        if time.time() - start_time > timeout_s:
            raise RuntimeError(f"Waiting to not get 202 from '{endpoint}' "
                               f"has reached a timeout ({timeout_s}s)") from None
        time.sleep(1)


def ml_wait_until_ml_is_available():
    endpoint = '/status'
    timeout_s = constants.AVAILABLE_SERVICE_TIMEOUT_S
    start_time = time.time()
    while True:
        try:
            res = ml_get(endpoint)
        except ConnectionError as e:
            if time.time() - start_time > timeout_s:
                pytest.exit(f"Waiting to get 200 from '{endpoint}' has reached a "
                            f"timeout ({timeout_s}s): {str(e)}", returncode=1)
            time.sleep(1)
            continue
        if res.status_code != HTTPStatus.OK or res.json()['status'] != 'OK':
            pytest.exit(f"Did not get 200 from '{endpoint}'. Received: {res.status_code}, {res.content}", returncode=1)
        break
