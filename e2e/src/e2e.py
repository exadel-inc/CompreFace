import pytest
from pymongo import MongoClient
from toolz import itertoolz

from .conftest import after_previous_gen, POST, DELETE, GET, _wait_until_training_completes, _wait_for_available_service
from .constants import ENV
from .sample_images import IMG_DIR

after_previous = after_previous_gen()

ML = ENV.ML_URL


@pytest.mark.run(order=next(after_previous))
def test_setup__drop_db():
    print(ENV.__str__())

    client = MongoClient(host=ENV.MONGO_HOST, port=ENV.MONGO_PORT)
    if ENV.MONGO_DBNAME in client.list_database_names() and 'tmp' in ENV.MONGO_DBNAME:
        client.drop_database(ENV.MONGO_DBNAME)
        print(f"Database drop: Successful")
    else:
        print("Database drop: Skipped")


@pytest.mark.run(order=next(after_previous))
def test__when_checking_status__then_returns_200():
    _wait_for_available_service(ML)

    res = GET(f"{ML}/status")

    assert res.status_code == 200, res.content
    assert res.json()['status'] == 'OK'


@pytest.mark.run(order=next(after_previous))
def test__when_opening_apidocs__then_returns_200():
    pass
    
    res = GET(f"{ML}/apidocs")

    assert res.status_code == 200, res.status_code


@pytest.mark.run(order=next(after_previous))
def test__when_retraining__then_returns_400():
    pass

    res = POST(f"{ML}/retrain", headers={'X-Api-Key': 'test-api-key'})

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: Not enough unique faces to start training a " \
                                    "new classifier model. Deleting existing classifiers, if any."


@pytest.mark.run(order=next(after_previous))
def test__given_no_api_key__when_adding_face__then_returns_401_unauthorized():
    files = {'file': open(IMG_DIR / 'personA-img1.jpg', 'rb')}

    res = POST(f"{ML}/faces/FAIL?retrain=no", files=files)

    assert res.status_code == 401, res.content


@pytest.mark.run(order=next(after_previous))
def test__given_img_with_no_faces__when_adding_face__then_returns_400_no_face_found():
    files = {'file': open(IMG_DIR / 'no-faces.jpg', 'rb')}

    res = POST(f"{ML}/faces/FAIL?retrain=no", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No face is found in the given image"


@pytest.mark.parametrize('file, name', [
    ('personA-img1.jpg', 'Marie Curie'),
    ('personB-img1.jpg', 'Stephen Hawking'),
    ('personC-img1.jpg', 'Paul Walker'), ])
@pytest.mark.run(order=next(after_previous))
def test__when_adding_face__then_returns_201(file, name):
    files = {'file': open(IMG_DIR / file, 'rb')}

    res = POST(f"{ML}/faces/{name}?retrain=no", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 201, res.content


@pytest.mark.run(order=next(after_previous))
def test__when_retraining__then_returns_202():
    pass

    res = POST(f"{ML}/retrain", headers={'X-Api-Key': 'test-api-key'})

    assert res.status_code == 202, res.content
    _wait_until_training_completes(ML)


@pytest.mark.run(order=next(after_previous))
def test__given_multiple_face_img__when_adding_face__then_returns_400_only_one_face_allowed():
    files = {'file': open(IMG_DIR / 'five-faces.jpg', 'rb')}

    res = POST(f"{ML}/faces/FAIL", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: Found more than one face in the given image"


# noinspection PyPep8Naming
@pytest.mark.run(order=next(after_previous))
def test__when_recognizing_faces__then_returns_face_A_name():
    files = {'file': open(IMG_DIR / 'personA-img2.jpg', 'rb')}

    res = POST(f"{ML}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 200, res.content
    result = res.json()['result']
    assert len(result) == 1
    assert result[0]['face_name'] == "Marie Curie"


@pytest.mark.run(order=next(after_previous))
def test__given_five_face_img__when_recognizing_faces__then_returns_five_distinct_results():
    file = {'file': open(IMG_DIR / 'five-faces.jpg', 'rb')}

    res = POST(f"{ML}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=file)

    assert res.status_code == 200, res.content
    result_items = res.json()['result']
    result_items_list = [tuple(item['box'].values()) for item in result_items]
    assert itertoolz.isdistinct(result_items_list), result_items
    assert len(result_items) == 5


@pytest.mark.run(order=next(after_previous))
def test__when_getting_names__then_returns_correct_names():
    pass

    res = GET(f"{ML}/faces", headers={'X-Api-Key': 'test-api-key'})

    result = res.json()['names']
    assert set(result) == {'Marie Curie', 'Stephen Hawking', 'Paul Walker'}


@pytest.mark.run(order=next(after_previous))
def test__given_other_api_key__when_getting_names__then_returns_no_names():
    pass

    res = GET(f"{ML}/faces", headers={'X-Api-Key': 'different-api-key'})

    result = res.json()['names']
    assert len(result) == 0


@pytest.mark.run(order=next(after_previous))
def test__when_deleting_face__then_returns_204():
    pass

    res_del = DELETE(f"{ML}/faces/Paul Walker", headers={'X-Api-Key': 'test-api-key'})

    assert res_del.status_code == 204, res_del.content
    _wait_until_training_completes(ML)


# noinspection PyPep8Naming
@pytest.mark.run(order=next(after_previous))
def test__when_recognizing_faces__then_only_faces_A_and_B_are_recognized():
    files_a = {'file': open(IMG_DIR / 'personA-img1.jpg', 'rb')}
    files_b = {'file': open(IMG_DIR / 'personB-img1.jpg', 'rb')}
    files_c = {'file': open(IMG_DIR / 'personC-img1.jpg', 'rb')}

    res_a = POST(f"{ML}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files_a)
    res_b = POST(f"{ML}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files_b)
    res_c = POST(f"{ML}/recognize", headers={'X-Api-Key': 'test-api-key'}, files=files_c)

    assert res_a.status_code == 200, res_a.content
    result_a = res_a.json()['result']
    assert result_a[0]['face_name'] == "Marie Curie"
    assert res_b.status_code == 200, res_b.content
    result_b = res_b.json()['result']
    assert result_b[0]['face_name'] == "Stephen Hawking"
    assert res_c.status_code == 200, res_a.content
    result_c = res_c.json()['result']
    assert not (result_c[0]['face_name'] == 'Paul Walker')
    _wait_until_training_completes(f"{ML}/retrain", check_result=False)


@pytest.mark.run(order=next(after_previous))
def test__when_getting_training_status__then_returns_200():
    pass

    res = GET(f"{ML}/retrain", headers={'X-Api-Key': 'test-api-key'})

    assert res.status_code == 200, res.content


# noinspection PyPep8Naming
@pytest.mark.run(order=next(after_previous))
def test__when_deleting_face_B_with_retraining__then_returns_204():
    pass

    res = DELETE(f"{ML}/faces/Stephen Hawking", headers={'X-Api-Key': 'test-api-key'})

    assert res.status_code == 204, res.content
    _wait_until_training_completes(f"{ML}/retrain", check_result=False)


@pytest.mark.run(order=next(after_previous))
def test__when_getting_training_status__then_returns_last_status_equals_error():
    pass

    res = GET(f"{ML}/retrain", headers={'X-Api-Key': 'test-api-key'})

    assert res.status_code == 200, res.content
    assert res.json()['last_status'] == 'ERROR'


@pytest.mark.run(order=next(after_previous))
def test__when_recognizing_faces__then_returns_400_no_classifier_trained():
    files = {'file': open(IMG_DIR / 'personA-img1.jpg', 'rb')}

    res = POST(f"{ML}/recognize?FAIL", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No classifier model is yet trained, " \
                                    "please train a classifier first"
