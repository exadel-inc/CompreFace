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

# TODO EFRS-42 Remove the use of 'await' parameter in all of the end-to-end tests once there is an official way for E2E tests to wait for the training to finish

import os
from http import HTTPStatus
from pathlib import Path

import pytest
import requests

from init_mongo_db import init_mongo_db
from src.storage.constants import MONGO_EFRS_DATABASE_NAME, MONGO_HOST, MONGO_PORT

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))

def wait_until_training_is_complete(host):

    res = requests.get(f"{host}/retrain", headers={'X-Api-Key': 'api-key-001'})
    while res.status_code != HTTPStatus.OK:
        res = requests.get(f"{host}/retrain", headers={'X-Api-Key': 'api-key-001'})
    return res.status_code == HTTPStatus.OK

@pytest.fixture
def host(request):
    return request.config.getoption('host')


@pytest.mark.first
def test_setup(request):
    if not request.config.getoption('drop-db'):
        return
    print("Dropping database...")
    from pymongo import MongoClient
    client = MongoClient(host=MONGO_HOST, port=MONGO_PORT)
    client.drop_database(MONGO_EFRS_DATABASE_NAME)
    print("Database dropped.")
    init_mongo_db()


def after_previous_gen():
    order_no = 1
    while True:
        yield order_no
        order_no += 1


after_previous = after_previous_gen()


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
def test__given_client_has_no_api_key__when_client_uploads_a_face_example__then_returns_400(host):
    files = {'file': open(CURRENT_DIR / 'files' / 'personA-img1.jpg', 'rb')}

    res = requests.post(f"{host}/faces/Marie Curie", headers={}, files=files)
    wait_until_training_is_complete(host)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == 'No API Key is given'


@pytest.mark.run(order=next(after_previous))
def test__when_client_uploads_a_face_example_without_faces__then_returns_400_no_face_found(host):
    files = {'file': open(CURRENT_DIR / 'files' / 'landscape.jpg', 'rb')}

    res = requests.post(f"{host}/faces/Marie Curie", headers={'X-Api-Key': 'api-key-001'}, files=files)
    wait_until_training_is_complete(host)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "No face is found in the given image"


@pytest.mark.run(order=next(after_previous))
def test__when_client_uploads_3_face_examples__then_returns_201(host):
    files_a = {'file': open(CURRENT_DIR / 'files' / 'personA-img1.jpg', 'rb')}
    files_b = {'file': open(CURRENT_DIR / 'files' / 'personB-img1.jpg', 'rb')}
    files_c = {'file': open(CURRENT_DIR / 'files' / 'personC-img1.jpg', 'rb')}

    res_a = requests.post(f"{host}/faces/Marie Curie?retrain=false",
                          headers={'X-Api-Key': 'api-key-001'}, files=files_a)
    res_b = requests.post(f"{host}/faces/Stephen Hawking?retrain=false",
                          headers={'X-Api-Key': 'test-api-key'}, files=files_b)
    res_c = requests.post(f"{host}/faces/Paul Walker?retrain=true",
                          headers={'X-Api-Key': 'test-api-key'}, files=files_c)
    wait_until_training_is_complete(host)

    assert res_a.status_code == 201, res_a.content
    assert res_b.status_code == 201, res_b.content
    assert res_c.status_code == 201, res_c.content


@pytest.mark.run(order=next(after_previous))
def test__given_api_key__when_client_asks_to_get_faces_list__then_returns_3_face_names_with_correct_values(host):
    pass

    res = requests.get(f"{host}/faces", headers={'X-Api-Key': 'test-api-key'})

    result = res.json()['names']
    assert len(result) == 3
    assert set(result) == {'Marie Curie', 'Stephen Hawking', 'Paul Walker'}


@pytest.mark.run(order=next(after_previous))
def test__given_other_api_key__when_client_asks_to_get_faces_list__then_returns_0_face_names(host):
    pass

    res = requests.get(f"{host}/faces", headers={'X-Api-Key': 'different-api-key'})

    result = res.json()['names']
    assert len(result) == 0


@pytest.mark.run(order=next(after_previous))
def test__when_client_requests_to_recognize_the_face_in_another_image__then_service_recognizes_it(host):
    files = {'file': open(CURRENT_DIR / 'files' / 'personA-img2.jpg', 'rb')}

    res = requests.post(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 200, res.content
    result = res.json()['result']
    assert len(result) == 1
    assert result[0]['face_name'] == "Marie Curie"


@pytest.mark.run(order=next(after_previous))
def test__when_client_tries_to_recognize_an_image_without_faces__then_returns_400_no_face_found(host):
    files = {'file': open(CURRENT_DIR / 'files' / 'landscape.jpg', 'rb')}

    res = requests.post(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "No face is found in the given image"


@pytest.mark.run(order=next(after_previous))
def test__when_client_deletes_person_c__then_returns_204(host):
    pass

    res_del = requests.delete(f"{host}/faces/Paul Walker?retrain=true",
                              headers={'X-Api-Key': 'test-api-key'})
    wait_until_training_is_complete(host)

    assert res_del.status_code == 204, res_del.content


@pytest.mark.run(order=next(after_previous))
def test__when_client_requests_to_recognize__then_only_persons_a_and_b_are_recognized(host):
    files_a = {'file': open(CURRENT_DIR / 'files' / 'personA-img1.jpg', 'rb')}
    files_b = {'file': open(CURRENT_DIR / 'files' / 'personB-img1.jpg', 'rb')}
    files_c = {'file': open(CURRENT_DIR / 'files' / 'personC-img1.jpg', 'rb')}

    res_a = requests.post(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files_a)
    res_b = requests.post(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files_b)
    res_c = requests.post(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files_c)

    assert res_a.status_code == 200, res_a.content
    result_a = res_a.json()['result']
    assert result_a[0]['face_name'] == "Marie Curie"
    assert res_b.status_code == 200, res_b.content
    result_b = res_b.json()['result']
    assert result_b[0]['face_name'] == "Stephen Hawking"
    assert res_c.status_code == 200, res_a.content
    result_c = res_c.json()['result']
    assert not (result_c[0]['face_name'] == 'Paul Walker')


@pytest.mark.run(order=next(after_previous))
def test__when_client_deletes_person_b__then_returns_204(host):
    pass

    res_del = requests.delete(f"{host}/faces/Stephen Hawking?retrain=true",
                              headers={'X-Api-Key': 'test-api-key'})
    wait_until_training_is_complete(host)

    assert res_del.status_code == 204, res_del.content


@pytest.mark.run(order=next(after_previous))
def test__requests_to_recognize_person_a__then_returns_500_no_models_found_for_api_key(host):
    files = {'file': open(CURRENT_DIR / 'files' / 'personA-img1.jpg', 'rb')}

    res = requests.post(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "No classifier model is yet trained for API key 'test-api-key'"
