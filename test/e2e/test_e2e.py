import pytest
import requests

from sample_images import IMG_DIR
from src.shared.utils.pytestutils import after_previous_gen
from src.shared.utils.pyutils import first_and_only
from ._expected_embedding import EXPECTED_EMBEDDING_FACENET2018

after_previous = after_previous_gen()


@pytest.fixture
def host(request):
    return request.config.getoption('host')


@pytest.mark.run(order=next(after_previous))
def test__when_status_checked__then_returns_200_with_ok(host):
    pass

    res = requests.get(f"{host}/status")

    assert res.status_code == 200, res.content
    assert res.json()['status'] == 'OK'


@pytest.mark.run(order=next(after_previous))
def test__when_apidocs_opened__then_returns_200(host):
    pass

    res = requests.get(f"{host}/apidocs")

    assert res.status_code == 200, res.status_code


@pytest.mark.run(order=next(after_previous))
def test__given_no_faces_img__when_scanned__then_returns_400_with_msg(host):
    files = {'file': open(IMG_DIR / 'no-faces.jpg', 'rb')}

    res = requests.post(f"{host}/scan_faces", files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "No face is found in the given image"


@pytest.mark.run(order=next(after_previous))
def test__given_img_with_face__when_scanned__then_returns_200_with_results(host):
    files = {'file': open(IMG_DIR / 'personA-img1.jpg', 'rb')}

    res = requests.post(f"{host}/scan_faces", files=files)

    assert res.status_code == 200, res.content
    assert res.json()['calculator_version'] == 'Facenet_v2018'
    face = first_and_only(res.json()['result'])
    assert _boxes_are_the_same(face['box'], {'x_max': 284, 'x_min': 146, 'y_max': 373, 'y_min': 193})
    assert _embeddings_are_the_same(face['embedding'], EXPECTED_EMBEDDING_FACENET2018)


def _embeddings_are_the_same(embedding1, embedding2):
    THRESHOLD = 0.01
    for i in range(len(embedding1)):
        if (embedding1[i] - embedding2[i]) / embedding2[i] > THRESHOLD:
            return False
    return True


def _boxes_are_the_same(box1, box2):
    ALLOWED_PX_DIFFERENCE = 10

    def value_is_the_same(key):
        return abs(box2[key] - box1[key]) <= ALLOWED_PX_DIFFERENCE

    return (value_is_the_same('x_max')
            and value_is_the_same('x_min')
            and value_is_the_same('y_max')
            and value_is_the_same('y_min'))
