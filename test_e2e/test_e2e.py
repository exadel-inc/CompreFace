"""
How to run:
1. Start the Face Recognition Service
2. Run command:
python -m pytest --host http://localhost:5000 test_e2e.py
"""
import os

import pytest
import requests

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))


@pytest.fixture
def host(request):
    return request.config.getoption('host')


def test_e2e(host):
    face_name = "Marie Curie"
    face_image1_filepath = f'{SCRIPT_DIR}/files/a1.jpg'
    face_image2_filepath = f'{SCRIPT_DIR}/files/a2.jpg'
    headers = {'X-Api-Key': 'valid-api-key'}

    # GIVEN Client uploads a face example
    files = {'file': open(face_image1_filepath, 'rb')}
    res = requests.post(f"{host}/faces/{face_name}", headers=headers, files=files)
    assert res.status_code == 201

    # WHEN Client requests to recognize the face in another image
    # THEN Service should recognize it
    files = {'file': open(face_image2_filepath, 'rb')}
    res = requests.post(f"{host}/recognize", headers=headers, files=files)
    assert res.status_code == 200
    result = res.json()['result']
    assert len(result) == 1
    assert result[0]['prediction'] == face_name

    # GIVEN Client deletes the face
    files = {'file': open(face_image2_filepath, 'rb')}
    res = requests.post(f"{host}/recognize", headers=headers, files=files)
    assert res.status_code == 204

    # WHEN Client requests to recognize the face in another image
    # THEN Service should not recognize it
    files = {'file': open(face_image2_filepath, 'rb')}
    res = requests.post(f"{host}/recognize", headers=headers, files=files)
    assert res.status_code == 200
    result = res.json()['result']
    assert len(result) == 0
