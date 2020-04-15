import pytest
from src.expected_embedding import EXPECTED_EMBEDDING
from src.ml_requests import ml_post
from src.ml_test_utils import embeddings_are_the_same, boxes_are_the_same
from src.sample_images import IMG_DIR
from src.test.init_test import after_previous

from src.constants import ALL_SCANNERS


@pytest.mark.run(order=next(after_previous))
def test__given_no_file__when_scanning__then_returns_400_bad_request():
    pass

    res = ml_post("/scan_faces")

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No file is attached"


@pytest.mark.run(order=next(after_previous))
def test__given_img_with_no_faces__when_scanning__then_returns_400_bad_request():
    files = {'file': open(IMG_DIR / '017_0.jpg', 'rb')}

    res = ml_post("/scan_faces", files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No face is found in the given image"


@pytest.mark.run(order=next(after_previous))
@pytest.mark.parametrize('filename', ['truncated.jpg', 'einstein.webp'])
def test__given_non_standard_img__when_scanning__then_returns_200(filename):
    files = {'file': open(IMG_DIR / filename, 'rb')}

    res = ml_post("/scan_faces", files=files)

    assert res.status_code == 200 and 'result' in res.json(), res.content


@pytest.mark.run(order=next(after_previous))
def test__given_img_with_face__when_scanning__then_returns_200_with_results():
    files = {'file': open(IMG_DIR / '007_B.jpg', 'rb')}

    res = ml_post("/scan_faces", files=files)

    assert res.status_code == 200, res.content
    calculator_version = res.json()['calculator_version']
    assert calculator_version in ALL_SCANNERS
    result = res.json()['result']
    assert len(result) == 1
    face = result[0]
    assert boxes_are_the_same(face['box'], {'x_max': 284, 'x_min': 146, 'y_max': 373, 'y_min': 193})
    assert embeddings_are_the_same(face['embedding'], EXPECTED_EMBEDDING[calculator_version]), face['embedding']
