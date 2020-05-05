from io import BytesIO

import imageio
import joblib
import pytest

from e2e_src.constants import ENV_E2E
from e2e_src.ml_requests import ml_get, ml_post
from e2e_src.sample_images import IMG_DIR
from e2e_src.test.init_test import after_previous


@pytest.mark.run(order=next(after_previous))
def test__when_checking_status__then_returns_200():
    pass

    res = ml_get("/status")

    assert res.status_code == 200, res.content
    assert res.json()['status'] == 'OK'


@pytest.mark.run(order=next(after_previous))
def test__when_opening_apidocs__then_returns_200():
    pass

    res = ml_get("/apidocs")

    assert res.status_code == 200, res.status_code


@pytest.mark.run(order=next(after_previous))
def test__when_opening_apidocs__then_returns_200():
    pass

    res = ml_get("/apidocs2")

    assert res.status_code == 200, res.status_code


@pytest.mark.run(order=next(after_previous))
def test__given_serialized_image_object__when_recognizing_faces__then_returns_200_and_results():
    with (IMG_DIR / '000_5.jpg.serialized').open('wb') as f:
        f.write(_serialize(_read_img(IMG_DIR / '000_5.jpg')))
    file = {'file': (IMG_DIR / '000_5.jpg.serialized').open('rb')}

    res = ml_post("/scan_faces", headers={'X-Api-Key': ENV_E2E.API_KEY}, files=file)

    assert res.status_code == 200, res.content
    assert len(res.json()['result']) == 5


def _serialize(obj):
    bytes_container = BytesIO()
    joblib.dump(obj, bytes_container)
    bytes_container.seek(0)
    bytes_data = bytes_container.read()
    return bytes_data


def _read_img(file):
    arr = imageio.imread(file)
    return arr[:, :, 0:3]
