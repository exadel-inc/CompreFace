import pytest

from ._expected_embeddings import EXPECTED_EMBEDDING_FACENET2018
from .conftest import after_previous_gen, POST, GET, _embeddings_are_the_same, _boxes_are_the_same, \
    _wait_for_available_service
from .sample_images import IMG_DIR

after_previous = after_previous_gen()


@pytest.mark.run(order=next(after_previous))
def test__when_checked_status__then_returns_200(host):
    _wait_for_available_service(host)

    res = GET(f"{host}/status")

    assert res.status_code == 200, res.content
    assert res.json()['status'] == 'OK'


@pytest.mark.run(order=next(after_previous))
def test__when_opened_apidocs__then_returns_200(host):
    pass
    res = GET(f"{host}/apidocs")

    assert res.status_code == 200, res.status_code


@pytest.mark.run(order=next(after_previous))
def test__given_no_file__when_scanning__then_returns_400_bad_request(host):
    pass

    res = POST(f"{host}/scan_faces", headers={'X-Api-Key': 'test-api-key'})

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No file is attached"


@pytest.mark.run(order=next(after_previous))
def test__given_img_with_no_faces__when_scanning__then_returns_400_bad_request(host):
    files = {'file': open(IMG_DIR / 'no-faces.jpg', 'rb')}

    res = POST(f"{host}/scan_faces", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No face is found in the given image"


@pytest.mark.run(order=next(after_previous))
def test__given_img_with_face__when_scanning__then_returns_200_with_results(host):
    files = {'file': open(IMG_DIR / 'personA-img1.jpg', 'rb')}

    res = POST(f"{host}/scan_faces", files=files)

    assert res.status_code == 200, res.content
    assert res.json()['calculator_version'] == 'Facenet_v2018'
    result = res.json()['result']
    assert len(result) == 1
    face = result[0]
    assert _boxes_are_the_same(face['box'], {'x_max': 284, 'x_min': 146, 'y_max': 373, 'y_min': 193})
    assert _embeddings_are_the_same(face['embedding'], EXPECTED_EMBEDDING_FACENET2018)
