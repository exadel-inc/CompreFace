import logging
import time
from http import HTTPStatus

import pytest
import requests
from pymongo import MongoClient
from requests import ReadTimeout

from .constants import ENV


def after_previous_gen():
    order_no = 1
    while True:
        yield order_no
        order_no += 1


def _request(method, endpoint, **kwargs):
    if 'timeout' not in kwargs or kwargs['timeout'] is None:
        kwargs['timeout'] = (ENV.CONNECT_TIMEOUT_S, ENV.READ_TIMEOUT_S)
    try:
        return requests.request(f"{ENV.ML_URL}{method}", endpoint, **kwargs)
    except requests.exceptions.ConnectionError as e:
        logging.error(str(e))
        raise ConnectionError(e) from None


# noinspection PyPep8Naming
def GET_ml(endpoint, **kwargs):
    return _request('get', endpoint, **kwargs)


# noinspection PyPep8Naming
def POST_ml(endpoint, **kwargs):
    return _request('post', endpoint, **kwargs)


# noinspection PyPep8Naming
def DELETE_ml(endpoint, **kwargs):
    return _request('delete', endpoint, **kwargs)


def wait_until_training_is_completed(api_key, check_response=True):
    time.sleep(2)
    endpoint = "/retrain"
    timeout_s = ENV.TRAINING_TIMEOUT_S
    start_time = time.time()
    while True:
        res = GET_ml(endpoint, headers={'X-Api-Key': api_key})
        if res.status_code != HTTPStatus.ACCEPTED:
            if check_response:
                assert res.status_code == HTTPStatus.OK
                assert res.json()['last_status'] == 'OK', res.content
            break
        if time.time() - start_time > timeout_s:
            raise Exception(f"Waiting to not get 202 from '{endpoint}' has reached a timeout ({timeout_s}s)") from None
        time.sleep(1)


def wait_until_ml_is_available():
    endpoint = '/status'
    timeout_s = ENV.AVAILABLE_SERVICE_TIMEOUT_S
    start_time = time.time()
    while True:
        try:
            res = GET_ml(endpoint)
        except (ConnectionError, ReadTimeout) as e:
            if time.time() - start_time > timeout_s:
                pytest.exit(f"Waiting to get 200 from '{endpoint}' has reached a "
                            f"timeout ({timeout_s}s): {str(e)}", returncode=1)
            time.sleep(1)
            continue
        assert res.status_code == HTTPStatus.OK, res.content
        break


def drop_db():
    client = MongoClient(host=ENV.MONGO_HOST, port=ENV.MONGO_PORT)
    if ENV.MONGO_DBNAME in client.list_database_names() and 'tmp' in ENV.MONGO_DBNAME:
        client.drop_database(ENV.MONGO_DBNAME)
        print(f"Database drop: Successful")
    else:
        print("Database drop: Skipped")
