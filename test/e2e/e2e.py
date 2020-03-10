"""
Usage:
python -m pytest e2e.py [--host <HOST:PORT>] [--drop-db]

Arguments:
    --host <HOST:PORT>      Run E2E against this host, default value: http://localhost:3000
    --drop-db               Drop and reinitialize database before E2E test

Instructions:
1. Start the Face Recognition Service
2. Run command, for example
python -m pytest e2e.py --host http://localhost:3000
"""

import os
import time
from http import HTTPStatus
from pathlib import Path

import pytest
import requests
from toolz import itertoolz

from init_mongo_db import init_mongo_db
from src.storage.constants import MONGO_EFRS_DATABASE_NAME, MONGO_HOST, MONGO_PORT

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
IMG_DIR = CURRENT_DIR / '_files'
TRAINING_TIMEOUT_S = 60


def _wait_until_training_is_complete(host):
    for _ in range(TRAINING_TIMEOUT_S):
        time.sleep(5)
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
def test__given_client_has_no_api_key__when_client_uploads_a_face_example__then_returns_400(host):
    files = {'file': open(IMG_DIR / 'e2e-personA-img1.jpg', 'rb')}

    res = requests.post(f"{host}/faces/Marie Curie", files=files)

    assert res.status_code == 400, res.content


@pytest.mark.run(order=next(after_previous))
def test__when_client_uploads_a_face_example_without_faces__then_returns_400_no_face_found(host):
    files = {'file': open(IMG_DIR / 'landscape.jpg', 'rb')}

    res = requests.post(f"{host}/faces/Marie Curie", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "No face is found in the given image"


@pytest.mark.run(order=next(after_previous))
def test__when_client_uploads_3_face_examples__then_returns_201(host):
    files_a = {'file': open(IMG_DIR / 'e2e-personA-img1.jpg', 'rb')}
    files_b = {'file': open(IMG_DIR / 'e2e-personB-img1.jpg', 'rb')}
    files_c = {'file': open(IMG_DIR / 'e2e-personC-img1.jpg', 'rb')}

    res_a = requests.post(f"{host}/faces/Marie Curie?retrain=no", headers={'X-Api-Key': 'test-api-key'},
                          files=files_a)
    res_b = requests.post(f"{host}/faces/Stephen Hawking?retrain=no", headers={'X-Api-Key': 'test-api-key'},
                          files=files_b)
    res_c = requests.post(f"{host}/faces/Paul Walker",
                          headers={'X-Api-Key': 'test-api-key'}, files=files_c)
    _wait_until_training_is_complete(host)

    assert res_a.status_code == 201, res_a.content
    assert res_b.status_code == 201, res_b.content
    assert res_c.status_code == 201, res_c.content


@pytest.mark.run(order=next(after_previous))
def test__when_client_asks_to_recognize_faces_in_5_person_jpg_image__then_returns_5_different_bounding_boxes(host):
    file = {'file': open(IMG_DIR / 'five-faces.jpg', 'rb')}

    res = requests.post(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=file)

    assert res.status_code == 200, res.content
    result_items = res.json()['result']
    result_items_list = [tuple(item['box'].values()) for item in result_items]
    assert itertoolz.isdistinct(result_items_list), result_items
    assert len(result_items) == 5


@pytest.mark.run(order=next(after_previous))
def test__when_client_asks_to_recognize_faces_in_5_person_png_image__then_returns_5_different_bounding_boxes(host):
    file = {'file': open(IMG_DIR / 'five-faces.png', 'rb')}

    res = requests.post(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=file)

    assert res.status_code == 200, res.content
    result_items = res.json()['result']
    result_items_list = [tuple(item['box'].values()) for item in result_items]
    assert itertoolz.isdistinct(result_items_list), result_items
    assert len(result_items) == 5


@pytest.mark.run(order=next(after_previous))
def test__when_client_tries_to_recognize_an_image_without_faces__then_returns_400_no_face_found(host):
    files = {'file': open(IMG_DIR / 'landscape.jpg', 'rb')}

    res = requests.post(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "No face is found in the given image"


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
    files = {'file': open(IMG_DIR / 'e2e-personA-img2.jpg', 'rb')}

    res = requests.post(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 200, res.content
    result = res.json()['result']
    assert len(result) == 1
    assert result[0]['face_name'] == "Marie Curie"


@pytest.mark.run(order=next(after_previous))
def test__when_client_deletes_person_c__then_returns_204(host):
    pass

    res_del = requests.delete(f"{host}/faces/Paul Walker", headers={'X-Api-Key': 'test-api-key'})
    _wait_until_training_is_complete(host)

    assert res_del.status_code == 204, res_del.content


@pytest.mark.run(order=next(after_previous))
def test__when_client_requests_to_recognize__then_only_persons_a_and_b_are_recognized(host):
    files_a = {'file': open(IMG_DIR / 'e2e-personA-img1.jpg', 'rb')}
    files_b = {'file': open(IMG_DIR / 'e2e-personB-img1.jpg', 'rb')}
    files_c = {'file': open(IMG_DIR / 'e2e-personC-img1.jpg', 'rb')}

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
@pytest.mark.skip('EFRS-249')
def test__when_client_uploads_big_image__then_returns_200(host):
    files = {'file': open(IMG_DIR / 'big-file.jpg', 'rb')}

    res = requests.post(f"{host}/faces/Tom Stevenson?retrain=no", headers={'X-Api-Key': 'test-api-key'}, files=files)
    _wait_until_training_is_complete(host)

    assert res.status_code == 201, res.content


@pytest.mark.run(order=next(after_previous))
def test__when_client_deletes_person_b__then_returns_204(host):
    pass

    res_del = requests.delete(f"{host}/faces/Stephen Hawking", headers={'X-Api-Key': 'test-api-key'})
    _wait_until_training_is_complete(host)

    assert res_del.status_code == 204, res_del.content


@pytest.mark.run(order=next(after_previous))
def test__requests_to_recognize_person_a__then_returns_500_no_models_found_for_api_key(host):
    files = {'file': open(IMG_DIR / 'e2e-personA-img1.jpg', 'rb')}

    res = requests.post(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "No classifier model is yet trained for API key 'test-api-key'"
