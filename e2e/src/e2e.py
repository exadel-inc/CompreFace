import time
from http import HTTPStatus

import pytest
import requests
from requests.exceptions import ConnectionError
from toolz import itertoolz

from .conftest import after_previous_gen, POST, DELETE, GET
from .sample_images import IMG_DIR

TRAINING_TIMEOUT_S = 15

after_previous = after_previous_gen()


@pytest.mark.run(order=next(after_previous))
def test__when_checking_status__then_returns_200(host):
    _wait_for_200(f"{host}/status", timeout_s=10)

    res = GET(f"{host}/status")

    assert res.status_code == 200, res.content
    assert res.json()['status'] == 'OK'


@pytest.mark.run(order=next(after_previous))
def test__when_opening_apidocs__then_returns_200(host):
    pass

    res = GET(f"{host}/apidocs")

    assert res.status_code == 200, res.status_code


@pytest.mark.run(order=next(after_previous))
def test__given_no_api_key__when_adding_face__then_returns_401_unauthorized(host):
    files = {'file': open(IMG_DIR / 'personA-img1.jpg', 'rb')}

    res = POST(f"{host}/faces/Marie Curie?retrain=no", files=files)

    assert res.status_code == 401, res.content


@pytest.mark.run(order=next(after_previous))
def test__given_img_with_no_faces__when_adding_face__then_returns_400_no_face_found(host):
    files = {'file': open(IMG_DIR / 'no-faces.jpg', 'rb')}

    res = POST(f"{host}/faces/Marie Curie?retrain=no", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No face is found in the given image"


@pytest.mark.parametrize('file, name',
                         [
                             ('personA-img1.jpg', 'Marie Curie'),
                             ('personB-img1.jpg', 'Stephen Hawking'),
                             ('personC-img1.jpg', 'Paul Walker'),
                             # ('personD-img1.jpg', 'Hans Bethe'),
                             # ('personD-img2.jpg', 'Hans Bethe'),
                             # ('personD-img3.jpg', 'Hans Bethe'),
                             # ('personD-img4.jpg', 'Hans Bethe')
                         ])
@pytest.mark.run(order=next(after_previous))
def test__when_adding_face__then_returns_201(host, file, name):
    files = {'file': open(IMG_DIR / file, 'rb')}

    res = POST(f"{host}/faces/{name}?retrain=no",
               headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 201, res.content


@pytest.mark.run(order=next(after_previous))
def test__when_retraining__then_returns_202(host):
    pass

    res = POST(f"{host}/retrain", headers={'X-Api-Key': 'test-api-key'})
    _wait_until_training_completes(host)

    assert res.status_code == 202, res.content


@pytest.mark.run(order=next(after_previous))
def test__given_multiple_face_img__when_adding_face__then_returns_400_only_one_face_allowed(host):
    files = {'file': open(IMG_DIR / 'five-faces.jpg', 'rb')}

    res = POST(f"{host}/faces/Marie Curie", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: Found more than one face in the given image"


@pytest.mark.run(order=next(after_previous))
def test__when_recognizing_faces__then_returns_face_A_name(host):
    files = {'file': open(IMG_DIR / 'personA-img2.jpg', 'rb')}

    res = POST(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 200, res.content
    result = res.json()['result']
    assert len(result) == 1
    assert result[0]['face_name'] == "Marie Curie"


@pytest.mark.run(order=next(after_previous))
def test__given_five_face_img__when_recognizing_faces__then_returns_five_distinct_results(host):
    file = {'file': open(IMG_DIR / 'five-faces.jpg', 'rb')}

    res = POST(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=file)

    assert res.status_code == 200, res.content
    result_items = res.json()['result']
    result_items_list = [tuple(item['box'].values()) for item in result_items]
    assert itertoolz.isdistinct(result_items_list), result_items
    assert len(result_items) == 5


@pytest.mark.run(order=next(after_previous))
def test__when_getting_names__then_returns_correct_names(host):
    pass

    res = GET(f"{host}/faces", headers={'X-Api-Key': 'test-api-key'})

    result = res.json()['names']
    assert set(result) == {'Marie Curie', 'Stephen Hawking', 'Paul Walker', 'Hans Bethe'}


@pytest.mark.run(order=next(after_previous))
def test__given_other_api_key__when_getting_names__then_returns_no_names(host):
    pass

    res = GET(f"{host}/faces", headers={'X-Api-Key': 'different-api-key'})

    result = res.json()['names']
    assert len(result) == 0


@pytest.mark.run(order=next(after_previous))
def test__when_deleting_face__then_returns_204(host):
    pass

    res_del = DELETE(f"{host}/faces/Paul Walker", headers={'X-Api-Key': 'test-api-key'})
    _wait_until_training_completes(host)

    assert res_del.status_code == 204, res_del.content


@pytest.mark.run(order=next(after_previous))
def test__when_recognizing_faces__then_only_faces_A_and_B_are_recognized(host):
    files_a = {'file': open(IMG_DIR / 'personA-img1.jpg', 'rb')}
    files_b = {'file': open(IMG_DIR / 'personB-img1.jpg', 'rb')}
    files_c = {'file': open(IMG_DIR / 'personC-img1.jpg', 'rb')}

    res_a = POST(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files_a)
    res_b = POST(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files_b)
    res_c = POST(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files_c)

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
def test__when_deleting_face_B__then_returns_204(host):
    pass

    res_del = DELETE(f"{host}/faces/Stephen Hawking", headers={'X-Api-Key': 'test-api-key'})
    _wait_until_training_completes(host)

    assert res_del.status_code == 204, res_del.content


@pytest.mark.run(order=next(after_previous))
def test__when_recognizing_faces__then_returns_400_no_classifier_trained(host):
    files = {'file': open(IMG_DIR / 'personA-img1.jpg', 'rb')}

    res = POST(f"{host}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No classifier model is yet trained, " \
                                    "please train a classifier first"


def _wait_for_200(url, timeout_s):
    elapsed_s = 0
    while True:
        try:
            assert requests.get(url, headers={'X-Api-Key': 'test-api-key'}).status_code == HTTPStatus.OK
        except (ConnectionError, AssertionError):
            elapsed_s += 1
            if elapsed_s > timeout_s:
                raise Exception(f"Waiting to get 200 from '{url}' has reached a timeout ({timeout_s}s)")
            elif elapsed_s % 10 == 0:
                print(f'\nWaiting >{elapsed_s}s ...')
            time.sleep(1)
            continue
        return


def _wait_until_training_completes(host):
    time.sleep(5)
    _wait_for_200(f"{host}/retrain", timeout_s=TRAINING_TIMEOUT_S)
