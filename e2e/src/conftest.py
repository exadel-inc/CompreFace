import logging
import time
from http import HTTPStatus

import pytest
import requests
from pymongo import MongoClient
from requests import ReadTimeout

from . import constants
from .constants import ENV


def after_previous_gen():
    order_no = 1
    while True:
        yield order_no
        order_no += 1


def _request(method, endpoint, **kwargs):
    if 'timeout' not in kwargs or kwargs['timeout'] is None:
        kwargs['timeout'] = (constants.CONNECT_TIMEOUT_S, constants.READ_TIMEOUT_S)
    try:
        return requests.request(method, f"{ENV.ML_URL}{endpoint}", **kwargs)
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
    timeout_s = constants.TRAINING_TIMEOUT_S
    start_time = time.time()
    while True:
        res = GET_ml(endpoint, headers={'X-Api-Key': api_key})
        if res.status_code != HTTPStatus.ACCEPTED:
            if check_response:
                assert res.status_code == HTTPStatus.OK
                assert res.json()['last_training_status'] == 'OK', res.content
            break
        if time.time() - start_time > timeout_s:
            raise Exception(f"Waiting to not get 202 from '{endpoint}' has reached a timeout ({timeout_s}s)") from None
        time.sleep(1)


def wait_until_ml_is_available():
    endpoint = '/status'
    timeout_s = constants.AVAILABLE_SERVICE_TIMEOUT_S
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


def drop_db_if_needed():
    client = MongoClient(ENV.MONGODB_HOST, ENV.MONGODB_PORT)
    if not ENV.DROP_DB:
        print(f"Skipping database drop: Variable 'DROP_DB' is set to false")
        return
    if ENV.MONGODB_DBNAME not in client.list_database_names():
        print(f"Skipping database drop: Database '{ENV.MONGODB_DBNAME}' not found")
        return
    if 'tmp' not in ENV.MONGODB_DBNAME:
        print(f"Skipping database drop: Database '{ENV.MONGODB_DBNAME}' is not a temporary database")
        return
    client.drop_database(ENV.MONGODB_DBNAME)
    print(f"Database drop: Successful")


def embeddings_are_the_same(embedding1, embedding2):
    for i in range(len(embedding1)):
        if (embedding1[i] - embedding2[i]) / embedding2[i] > constants.EMB_SIMILARITY_THRESHOLD:
            return False
    return True


def boxes_are_the_same(box1, box2):
    def value_is_the_same(key):
        return abs(box2[key] - box1[key]) <= constants.BBOX_ALLOWED_PX_DIFFERENCE

    return (value_is_the_same('x_max')
            and value_is_the_same('x_min')
            and value_is_the_same('y_max')
            and value_is_the_same('y_min'))
