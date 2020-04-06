import pytest
from toolz import itertoolz

from ._expected_embeddings import EXPECTED_EMBEDDING_FACENET2018
from .conftest import (after_previous_gen, POST_ml, DELETE_ml, GET_ml, wait_until_training_is_completed,
                       wait_until_ml_is_available, drop_db, boxes_are_the_same, embeddings_are_the_same)
from .constants import ENV
from .sample_images import IMG_DIR

after_previous = after_previous_gen()

ML = ENV.ML_URL


@pytest.mark.run(order=next(after_previous))
def test_init():
    print(ENV.__str__())
    if ENV.DROP_DB:
        drop_db()
    wait_until_ml_is_available()


@pytest.mark.run(order=next(after_previous))
def test__when_checking_status__then_returns_200():
    pass

    res = GET_ml("/status")

    assert res.status_code == 200, res.content
    assert res.json()['status'] == 'OK'


@pytest.mark.run(order=next(after_previous))
def test__when_opening_apidocs__then_returns_200():
    pass

    res = GET_ml("/apidocs")

    assert res.status_code == 200, res.status_code


@pytest.mark.run(order=next(after_previous))
def test__when_opening_apidocs__then_returns_200():
    pass

    res = GET_ml("/apidocs2")

    assert res.status_code == 200, res.status_code


@pytest.mark.run(order=next(after_previous))
def test__when_retraining__then_returns_400():
    pass

    res = POST_ml("/retrain", headers={'X-Api-Key': ENV.API_KEY})

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: Not enough unique faces to start training a " \
                                    "new classifier model. Deleting existing classifiers, if any."


@pytest.mark.run(order=next(after_previous))
def test__given_img_with_no_faces__when_adding_face__then_returns_400_no_face_found():
    files = {'file': open(IMG_DIR / '017_0.jpg', 'rb')}

    res = POST_ml("/faces/JoeSmith", headers={'X-Api-Key': ENV.API_KEY}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No face is found in the given image"


@pytest.mark.parametrize('file, name', [
    ('001_A.jpg', 'Marie Curie'),
    ('007_B.jpg', 'Stephen Hawking'),
    ('009_C.jpg', 'Paul Walker'), ])
@pytest.mark.run(order=next(after_previous))
def test__when_adding_face__then_returns_201(file, name):
    files = {'file': open(IMG_DIR / file, 'rb')}

    res = POST_ml(f"/faces/{name}?retrain=no", headers={'X-Api-Key': ENV.API_KEY}, files=files)

    assert res.status_code == 201, res.content


@pytest.mark.run(order=next(after_previous))
def test__when_retraining__then_returns_202():
    pass

    res = POST_ml("/retrain", headers={'X-Api-Key': ENV.API_KEY})

    assert res.status_code == 202, res.content
    wait_until_training_is_completed(ENV.API_KEY)


@pytest.mark.run(order=next(after_previous))
def test__given_multiple_face_img__when_adding_face__then_returns_400_only_one_face_allowed():
    files = {'file': open(IMG_DIR / '000_5.jpg', 'rb')}

    res = POST_ml("/faces/JoeSmith", headers={'X-Api-Key': ENV.API_KEY}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: Found more than one face in the given image"


# noinspection PyPep8Naming
@pytest.mark.run(order=next(after_previous))
def test__when_recognizing_faces__then_returns_face_A_name():
    files = {'file': open(IMG_DIR / '002_A.jpg', 'rb')}

    res = POST_ml("/recognize", headers={'X-Api-Key': ENV.API_KEY}, files=files)

    assert res.status_code == 200, res.content
    result = res.json()['result']
    assert len(result) == 1
    assert result[0]['face_name'] == "Marie Curie"


@pytest.mark.run(order=next(after_previous))
def test__given_five_face_img__when_recognizing_faces__then_returns_five_distinct_results():
    file = {'file': open(IMG_DIR / '000_5.jpg', 'rb')}

    res = POST_ml("/recognize", headers={'X-Api-Key': ENV.API_KEY}, files=file)

    assert res.status_code == 200, res.content
    result_items = res.json()['result']
    result_items_list = [tuple(item['box'].values()) for item in result_items]
    assert itertoolz.isdistinct(result_items_list), result_items
    assert len(result_items) == 5


@pytest.mark.run(order=next(after_previous))
def test__when_getting_names__then_returns_correct_names():
    pass

    res = GET_ml("/faces", headers={'X-Api-Key': ENV.API_KEY})

    result = res.json()['names']
    assert set(result) == {'Marie Curie', 'Stephen Hawking', 'Paul Walker'}


@pytest.mark.run(order=next(after_previous))
def test__given_other_api_key__when_getting_names__then_returns_no_names():
    pass

    res = GET_ml("/faces", headers={'X-Api-Key': f'{ENV.API_KEY}-different'})

    result = res.json()['names']
    assert len(result) == 0


@pytest.mark.run(order=next(after_previous))
def test__when_deleting_face__then_returns_204():
    pass

    res_del = DELETE_ml("/faces/Paul Walker", headers={'X-Api-Key': ENV.API_KEY})

    assert res_del.status_code == 204, res_del.content
    wait_until_training_is_completed(ENV.API_KEY)


# noinspection PyPep8Naming
@pytest.mark.run(order=next(after_previous))
def test__when_recognizing_faces__then_only_faces_A_and_B_are_recognized():
    files_a = {'file': open(IMG_DIR / '002_A.jpg', 'rb')}
    files_b = {'file': open(IMG_DIR / '008_B.jpg', 'rb')}
    files_c = {'file': open(IMG_DIR / '009_C.jpg', 'rb')}

    res_a = POST_ml("/recognize", headers={'X-Api-Key': ENV.API_KEY}, files=files_a)
    res_b = POST_ml("/recognize", headers={'X-Api-Key': ENV.API_KEY}, files=files_b)
    res_c = POST_ml("/recognize", headers={'X-Api-Key': ENV.API_KEY}, files=files_c)

    assert res_a.status_code == 200, res_a.content
    result_a = res_a.json()['result']
    assert result_a[0]['face_name'] == "Marie Curie"
    assert res_b.status_code == 200, res_b.content
    result_b = res_b.json()['result']
    assert result_b[0]['face_name'] == "Stephen Hawking"
    assert res_c.status_code == 200, res_a.content
    result_c = res_c.json()['result']
    assert not (result_c[0]['face_name'] == 'Paul Walker')
    wait_until_training_is_completed(ENV.API_KEY, check_response=False)


@pytest.mark.run(order=next(after_previous))
def test__when_getting_training_status__then_returns_200():
    pass

    res = GET_ml("/retrain", headers={'X-Api-Key': ENV.API_KEY})

    assert res.status_code == 200, res.content


# noinspection PyPep8Naming
@pytest.mark.run(order=next(after_previous))
def test__when_deleting_face_B_with_retraining__then_returns_204():
    pass

    res = DELETE_ml("/faces/Stephen Hawking", headers={'X-Api-Key': ENV.API_KEY})

    assert res.status_code == 204, res.content
    wait_until_training_is_completed(ENV.API_KEY, check_response=False)


@pytest.mark.run(order=next(after_previous))
def test__when_getting_training_status__then_returns_last_training_status_equals_error():
    pass

    res = GET_ml("/retrain", headers={'X-Api-Key': ENV.API_KEY})

    assert res.status_code == 200, res.content
    assert res.json()['last_training_status'] == 'ERROR'


@pytest.mark.run(order=next(after_previous))
def test__when_recognizing_faces__then_returns_400_no_classifier_trained():
    files = {'file': open(IMG_DIR / '017_0.jpg', 'rb')}

    res = POST_ml("/recognize", headers={'X-Api-Key': ENV.API_KEY}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No classifier model is yet trained, " \
                                    "please train a classifier first"


@pytest.mark.run(order=next(after_previous))
def test__given_no_file__when_scanning__then_returns_400_bad_request():
    pass

    res = POST_ml("/scan_faces", headers={'X-Api-Key': 'test-api-key'})

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No file is attached"


@pytest.mark.run(order=next(after_previous))
def test__given_img_with_no_faces__when_scanning__then_returns_400_bad_request():
    files = {'file': open(IMG_DIR / 'no-faces.jpg', 'rb')}

    res = POST_ml("/scan_faces", headers={'X-Api-Key': 'test-api-key'}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No face is found in the given image"


@pytest.mark.run(order=next(after_previous))
def test__given_img_with_face__when_scanning__then_returns_200_with_results():
    files = {'file': open(IMG_DIR / '007_B.jpg', 'rb')}

    res = POST_ml("/scan_faces", files=files)

    assert res.status_code == 200, res.content
    assert res.json()['calculator_version'] == 'Facenet2018'
    result = res.json()['result']
    assert len(result) == 1
    face = result[0]
    assert boxes_are_the_same(face['box'], {'x_max': 284, 'x_min': 146, 'y_max': 373, 'y_min': 193})
    assert embeddings_are_the_same(face['embedding'], EXPECTED_EMBEDDING_FACENET2018)
