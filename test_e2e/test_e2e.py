"""
Usage:
python -m pytest test_e2e.py [--host <HOST:PORT>] [--drop-db]

Arguments:
    --host <HOST:PORT>      Run E2E against this host, default value: http://localhost:5001
    --drop-db               Drop and reinitialize database before E2E test

Instructions:
1. Start the Face Recognition Service
2. Run command, for example
python -m pytest test_e2e.py --host http://localhost:5001
"""

import os
import time
from http import HTTPStatus
from pathlib import Path

import pytest
import requests

from init_mongo_db import init_mongo_db
from main import ROOT_DIR
from src.storage.constants import MONGO_EFRS_DATABASE_NAME, MONGO_HOST, MONGO_PORT

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
IMG_DIR = ROOT_DIR / 'test_files'
TRAINING_TIMEOUT_S = 60


def _wait_until_training_is_complete(host):
    for _ in range(TRAINING_TIMEOUT_S):
        time.sleep(1)
        res = requests.get(f"{host}/retrain", headers={'X-Api-Key': 'test-api-key'})
        if res.status_code == HTTPStatus.OK:
            return
    raise Exception("Waiting for classifier training completion has reached a timeout")


@pytest.fixture
def host(request):
    return request.config.getoption('host')


def after_previous_gen():
    order_no = 1
    while True:
        yield order_no
        order_no += 1


after_previous = after_previous_gen()


@pytest.mark.run(order=next(after_previous))
def test_setup__drop_db(request):
    if not request.config.getoption('drop-db'):
        return
    print("Dropping database...")
    from pymongo import MongoClient
    client = MongoClient(host=MONGO_HOST, port=MONGO_PORT)
    client.drop_database(MONGO_EFRS_DATABASE_NAME)
    print("Database dropped.")
    init_mongo_db()


@pytest.mark.run(order=next(after_previous))
def test__when_client_checks_service_availability__returns_200(host):
    pass

    res = requests.get(f"{host}/status")

    assert res.status_code == 200, res.content
    assert res.json()['status'] == 'OK'


@pytest.mark.run(order=next(after_previous))
def test__when_client_opens_apidocs__returns_200(host):
    pass

    res = requests.get(f"{host}/apidocs")

    assert res.status_code == 200, res.status_code


@pytest.mark.run(order=next(after_previous))
def test__when_client_tries_to_recognize_an_image_without_faces__then_returns_400_no_face_found(host):
    # TODO EFRS-103 fix this test
    files = {'file': open(IMG_DIR / 'landscape.jpg', 'rb')}

    res = requests.post(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "No face is found in the given image"


@pytest.mark.run(order=next(after_previous))
def test__when_client_requests_to_recognize__then_only_persons_a_and_b_are_recognized(host):
    # TODO EFRS-103 fix this test
    files_a = {'file': open(IMG_DIR / 'e2e-personA-img1.jpg', 'rb')}

    res_a = requests.post(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files_a)

    assert res_a.status_code == 200, res_a.content
    result_a = res_a.json()['result']
    assert result_a[0]['face_name'] == "Marie Curie"
