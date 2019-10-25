"""
Usage:
python -m pytest [--host <HOST>] test_e2e.py

Arguments:
    <HOST>  Host of the service, default value: http://localhost:5001

Instructions:
1. Start the Face Recognition Service
2. Run command, for example
python -m pytest --host http://localhost:5001 test_e2e.py
"""

# TODO EGP-708 Remove the use of 'await' parameter in all of the end-to-end tests once there is an official way for E2E tests to wait for the training to finish

import os
from pathlib import Path

import pytest
import requests

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))


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
def test__when_client_checks_service_availability__returns_200(host):
    pass

    res = requests.get(f"{host}/status")

    assert res.status_code == 200, res.content
    assert res.json()['status'] == 'OK'


@pytest.mark.run(order=next(after_previous))
def test__when_client_opens_apidocs__returns_200(host):
    pass

    res = requests.get(f"{host}/apidocs")

    assert res.status_code == 200, res.content


@pytest.mark.run(order=next(after_previous))
def test__given_client_has_invalid_api_key__when_client_uploads_a_face_example__then_returns_401(host):
    files = {'file': open(CURRENT_DIR / 'files' / 'personA-img1.jpg', 'rb')}

    res = requests.post(f"{host}/faces/Marie Curie", headers={'X-Api-Key': 'invalid-api-key'}, files=files)

    assert res.status_code == 401, res.content


@pytest.mark.run(order=next(after_previous))
def test__when_client_uploads_3_face_examples__then_returns_201(host):
    files_a = {'file': open(CURRENT_DIR / 'files' / 'personA-img1.jpg', 'rb')}
    files_b = {'file': open(CURRENT_DIR / 'files' / 'personB-img1.jpg', 'rb')}
    files_c = {'file': open(CURRENT_DIR / 'files' / 'personC-img1.jpg', 'rb')}

    res_a = requests.post(f"{host}/faces/Marie Curie?retrain=false",
                          headers={'X-Api-Key': 'valid-api-key'}, files=files_a)
    res_b = requests.post(f"{host}/faces/Stephen Hawking?retrain=false",
                          headers={'X-Api-Key': 'valid-api-key'}, files=files_b)
    res_c = requests.post(f"{host}/faces/Paul Walker?retrain=true&await=true",
                          headers={'X-Api-Key': 'valid-api-key'}, files=files_c)

    assert res_a.status_code == 201, res_a.content
    assert res_b.status_code == 201, res_b.content
    assert res_c.status_code == 201, res_c.content


@pytest.mark.run(order=next(after_previous))
def test__given_api_key__when_client_asks_to_get_faces_list__then_returns_3_face_names_with_correct_values(host):
    ...  # TODO EGP-687


@pytest.mark.run(order=next(after_previous))
def test__given_other_api_key__when_client_asks_to_get_faces_list__then_returns_0_face_names(host):
    ...  # TODO EGP-687


@pytest.mark.run(order=next(after_previous))
def test__when_client_requests_to_recognize_the_face_in_another_image__then_service_recognizes_it(host):
    files = {'file': open(CURRENT_DIR / 'files' / 'personA-img2.jpg', 'rb')}

    res = requests.post(f"{host}/recognize", headers={'X-Api-Key': 'valid-api-key'}, files=files)

    assert res.status_code == 200, res.content
    result = res.json()['result']
    assert len(result) == 1
    assert result[0]['prediction'] == "Marie Curie"


@pytest.mark.run(order=next(after_previous))
def test__when_client_deletes_person_c__then_returns_204_and_only_persons_a_and_b_are_recognized(host):
    ...  # TODO EGP-687, call retrain retrain?await=true endpoint after deletion


@pytest.mark.run(order=next(after_previous))
def test__when_client_deletes_person_b__then_returns_204(host):
    ...  # TODO EGP-687, call retrain retrain?await=true endpoint after deletion


@pytest.mark.run(order=next(after_previous))
def test__requests_to_recognize_person_a__then_returns_500_no_models_found_for_api_key(host):
    ...  # TODO EGP-687, call retrain retrain?await=true endpoint after deletion
