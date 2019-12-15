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
from pathlib import Path

import pytest
import requests

from main import ROOT_DIR
from test_e2e.constants import EXPECTED_EMBEDDING

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
IMG_DIR = ROOT_DIR / 'test_files'
TRAINING_TIMEOUT_S = 60


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

    assert res.status_code == 200, res.status_code


@pytest.mark.run(order=next(after_previous))
def test__when_client_tries_to_scan_an_image_without_faces__then_returns_400_no_face_found(host):
    files = {'file': open(IMG_DIR / 'landscape.jpg', 'rb')}

    res = requests.post(f"{host}/scan_faces", files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "No face is found in the given image"


@pytest.mark.run(order=next(after_previous))
def test__when_client_requests_to_scan_face__then_correct_box_and_embedding_returned(host):
    files = {'file': open(IMG_DIR / 'e2e-personA-img1.jpg', 'rb')}

    res = requests.post(f"{host}/scan_faces", files=files)

    assert res.status_code == 200, res.content
    assert res.json()['calculator_version'] == "embedding_calc_model_20170512.pb"
    assert len(res.json()['result']) == 1
    result_item = res.json()['result'][0]
    assert _boxes_are_the_same(result_item['box'], {'x_max': 284, 'x_min': 146, 'y_max': 373, 'y_min': 193})
    assert _embeddings_are_the_same(result_item['embedding'], EXPECTED_EMBEDDING)
    assert result_item['box']['probability'] > 0.9


def _embeddings_are_the_same(embedding1, embedding2):
    threshold = 0.01
    for i in range(len(embedding1)):
        if (embedding1[i] - embedding2[i]) / embedding2[i] > threshold:
            return False
    return True


def _boxes_are_the_same(box1, box2):
    def value_is_the_same(key):
        allowed_px_diff = 10
        return abs(box2[key] - box1[key]) < allowed_px_diff

    return (value_is_the_same('x_max')
            and value_is_the_same('x_min')
            and value_is_the_same('y_max')
            and value_is_the_same('y_min'))
