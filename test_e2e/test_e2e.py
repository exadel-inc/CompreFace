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
import os
import time
from pathlib import Path

import pytest
import requests

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
AUTH_HEADERS = {'X-Api-Key': 'valid-api-key'}


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

    assert res.status_code == 200
    assert res.json()['status'] == 'OK'


@pytest.mark.run(order=next(after_previous))
def test__when_client_opens_apidocs__returns_200(host):
    pass

    res = requests.get(f"{host}/apidocs")

    assert res.status_code == 200


@pytest.mark.run(order=next(after_previous))
def test__given_client_has_invalid_api_key__when_client_uploads_a_face_example__then_returns_401(host):
    files_a = {'file': open(CURRENT_DIR / 'files' / 'personA-img1.jpg', 'rb')}

    res_a = requests.post(f"{host}/faces/Marie Curie?retrain=false",
                          headers={'X-Api-Key': 'invalid-api-key'}, files=files_a)

    assert res_a.status_code == 401


@pytest.mark.run(order=next(after_previous))
def test__when_client_uploads_a_face_example__then_returns_201(host):
    files_a = {'file': open(CURRENT_DIR / 'files' / 'personA-img1.jpg', 'rb')}
    files_b = {'file': open(CURRENT_DIR / 'files' / 'personB-img1.jpg', 'rb')}

    res_a = requests.post(f"{host}/faces/Marie Curie?retrain=false", headers=AUTH_HEADERS, files=files_a)
    res_b = requests.post(f"{host}/faces/Stephen Hawking", headers=AUTH_HEADERS, files=files_b)

    assert res_a.status_code == 201
    assert res_b.status_code == 201

    time.sleep(20)


@pytest.mark.run(order=next(after_previous))
def test__when_client_requests_to_recognize_the_face_in_another_image__then_service_recognizes_it(host):
    files = {'file': open(CURRENT_DIR / 'files' / 'personA-img2.jpg', 'rb')}

    res = requests.post(f"{host}/recognize", headers=AUTH_HEADERS, files=files)

    assert res.status_code == 200
    result = res.json()['result']
    assert len(result) == 1
    assert result[0]['prediction'] == "Marie Curie"
