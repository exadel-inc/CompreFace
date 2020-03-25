import time
from http import HTTPStatus
from typing import Union

import pytest
from requests import ReadTimeout
from toolz import itertoolz

from .conftest import after_previous_gen, POST, DELETE, GET
from .constants import MONGO_HOST, MONGO_PORT, MONGO_EFRS_DATABASE_NAME, DO_DROP_DB, TIMEOUT_MULTIPLIER
from .sample_images import IMG_DIR

after_previous = after_previous_gen()

AVAILABLE_SERVICE_TIMEOUT_S = TIMEOUT_MULTIPLIER * 8
TRAINING_TIMEOUT_S = TIMEOUT_MULTIPLIER * 30


@pytest.mark.run(order=next(after_previous))
def test_setup__drop_db():
    if not DO_DROP_DB:
        return
    print(f"Dropping database {MONGO_EFRS_DATABASE_NAME}@{MONGO_HOST}:{MONGO_PORT}...")
    from pymongo import MongoClient
    client = MongoClient(host=MONGO_HOST, port=MONGO_PORT)
    client.drop_database(MONGO_EFRS_DATABASE_NAME)
    print(f"Successfully dropped database {MONGO_EFRS_DATABASE_NAME}@{MONGO_HOST}:{MONGO_PORT}")


@pytest.mark.run(order=next(after_previous))
def test__when_checking_status__then_returns_200(host):
    _wait_for_available_service(host)

    res = GET(f"{host}/status")

    assert res.status_code == 200, res.content
    assert res.json()['status'] == 'OK'


@pytest.mark.run(order=next(after_previous))
def test__when_opening_apidocs__then_returns_200(host):
    pass
    res = GET(f"{host}/apidocs")

    assert res.status_code == 200, res.status_code


@pytest.mark.run(order=next(after_previous))
def test__when_retraining__then_returns_400(host):
    pass

    res = POST(f"{host}/retrain", headers={'X-Api-Key': 'test-api-key'})

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: Not enough unique faces to start training a " \
                                    "new classifier model. Deleting existing classifiers, if any."


@pytest.mark.run(order=next(after_previous))
def test__given_no_api_key__when_adding_face__then_returns_401_unauthorized(host):
    files = {'file': open(IMG_DIR / 'personA-img1.jpg', 'rb')}

    res = POST(f"{host}/faces/FAIL?retrain=no", files=files)

    assert res.status_code == 401, res.content


@pytest.mark.run(order=next(after_previous))
def test__given_img_with_no_faces__when_adding_face__then_returns_400_no_face_found(host):
    files = {'file': open(IMG_DIR / 'no-faces.jpg', 'rb')}

    res = POST(f"{host}/faces/FAIL?retrain=no", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No face is found in the given image"


@pytest.mark.parametrize('file, name', [
    ('personA-img1.jpg', 'Marie Curie'),
    ('personB-img1.jpg', 'Stephen Hawking'),
    ('personC-img1.jpg', 'Paul Walker'), ])
@pytest.mark.run(order=next(after_previous))
def test__when_adding_face__then_returns_201(host, file, name):
    files = {'file': open(IMG_DIR / file, 'rb')}

    res = POST(f"{host}/faces/{name}?retrain=no", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 201, res.content


@pytest.mark.run(order=next(after_previous))
def test__when_retraining__then_returns_202(host):
    pass

    res = POST(f"{host}/retrain", headers={'X-Api-Key': 'test-api-key'})

    assert res.status_code == 202, res.content
    _wait_until_training_completes(host)


@pytest.mark.run(order=next(after_previous))
def test__given_multiple_face_img__when_adding_face__then_returns_400_only_one_face_allowed(host):
    files = {'file': open(IMG_DIR / 'five-faces.jpg', 'rb')}

    res = POST(f"{host}/faces/FAIL", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: Found more than one face in the given image"


# noinspection PyPep8Naming
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
    assert set(result) == {'Marie Curie', 'Stephen Hawking', 'Paul Walker'}


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

    assert res_del.status_code == 204, res_del.content
    _wait_until_training_completes(host)


# noinspection PyPep8Naming
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
    _wait_until_training_completes(f"{host}/retrain", expected_code=None)


@pytest.mark.run(order=next(after_previous))
def test__when_getting_training_status__then_returns_200(host):
    pass

    res = GET(f"{host}/retrain", headers={'X-Api-Key': 'test-api-key'})

    assert res.status_code == 200, res.content


# noinspection PyPep8Naming
@pytest.mark.run(order=next(after_previous))
def test__when_deleting_face_B_with_retraining__then_returns_204(host):
    pass

    res = DELETE(f"{host}/faces/Stephen Hawking", headers={'X-Api-Key': 'test-api-key'})

    assert res.status_code == 204, res.content
    _wait_until_training_completes(f"{host}/retrain", expected_code=None)


@pytest.mark.run(order=next(after_previous))
def test__when_getting_training_status__then_returns_500(host):
    pass

    res = GET(f"{host}/retrain", headers={'X-Api-Key': 'test-api-key'})

    assert res.status_code == 500, res.content


@pytest.mark.run(order=next(after_previous))
def test__when_recognizing_faces__then_returns_400_no_classifier_trained(host):
    files = {'file': open(IMG_DIR / 'personA-img1.jpg', 'rb')}

    res = POST(f"{host}/recognize?FAIL", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No classifier model is yet trained, " \
                                    "please train a classifier first"


def _wait_for_available_service(host):
    url = f"{host}/status"
    timeout_s = AVAILABLE_SERVICE_TIMEOUT_S
    start_time = time.time()
    while True:
        try:
            res = GET(url, headers={'X-Api-Key': 'test-api-key'})
        except (ConnectionError, ReadTimeout) as e:
            if time.time() - start_time > timeout_s:
                raise Exception(f"Waiting to get 200 from '{url}' has reached a "
                                f"timeout ({timeout_s}s): {str(e)}") from None
            time.sleep(1)
            continue
        assert res.status_code == HTTPStatus.OK, res.content
        break


def _wait_until_training_completes(host, expected_code: Union[HTTPStatus, None] = HTTPStatus.OK):
    time.sleep(2)
    url = f"{host}/retrain"
    timeout_s = TRAINING_TIMEOUT_S
    start_time = time.time()
    while True:
        res = GET(url, headers={'X-Api-Key': 'test-api-key'})
        if res.status_code != HTTPStatus.ACCEPTED:
            if expected_code:
                assert res.status_code == expected_code
            break
        if time.time() - start_time > timeout_s:
            raise Exception(f"Waiting to not get 202 from '{url}' has reached a timeout ({timeout_s}s)") from None
        time.sleep(1)
