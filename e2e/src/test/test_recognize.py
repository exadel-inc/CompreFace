import pytest
from src.ml_requests import ml_wait_until_training_is_completed, ml_get, ml_post, ml_delete
from src.sample_images import IMG_DIR
from src.test.init_test import after_previous
from toolz import itertoolz

from src.constants import ENV_E2E


@pytest.mark.run(order=next(after_previous))
def test__when_retraining__then_returns_400():
    pass

    res = ml_post("/retrain", headers={'X-Api-Key': ENV_E2E.API_KEY})

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: Not enough unique faces to start training a " \
                                    "new classifier model. Deleting existing classifiers, if any."


@pytest.mark.run(order=next(after_previous))
def test__given_img_with_no_faces__when_adding_face__then_returns_400_no_face_found():
    files = {'file': open(IMG_DIR / '017_0.jpg', 'rb')}

    res = ml_post("/faces/JoeSmith", headers={'X-Api-Key': ENV_E2E.API_KEY}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: No face is found in the given image"


@pytest.mark.parametrize('file, name', [
    ('001_A.jpg', 'Marie Curie'),
    ('007_B.jpg', 'Stephen Hawking'),
    ('009_C.jpg', 'Paul Walker'), ])
@pytest.mark.run(order=next(after_previous))
def test__when_adding_face__then_returns_201(file, name):
    files = {'file': open(IMG_DIR / file, 'rb')}

    res = ml_post(f"/faces/{name}?retrain=no", headers={'X-Api-Key': ENV_E2E.API_KEY}, files=files)

    assert res.status_code == 201, res.content


@pytest.mark.run(order=next(after_previous))
def test__when_retraining__then_returns_202():
    pass

    res = ml_post("/retrain", headers={'X-Api-Key': ENV_E2E.API_KEY})

    assert res.status_code == 202, res.content
    ml_wait_until_training_is_completed(ENV_E2E.API_KEY)


@pytest.mark.run(order=next(after_previous))
def test__given_multiple_face_img__when_adding_face__then_returns_400_only_one_face_allowed():
    files = {'file': open(IMG_DIR / '000_5.jpg', 'rb')}

    res = ml_post("/faces/JoeSmith", headers={'X-Api-Key': ENV_E2E.API_KEY}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "400 Bad Request: Found more than one face in the given image"


# noinspection PyPep8Naming
@pytest.mark.run(order=next(after_previous))
def test__when_recognizing_faces__then_returns_face_A_name():
    files = {'file': open(IMG_DIR / '002_A.jpg', 'rb')}

    res = ml_post("/recognize", headers={'X-Api-Key': ENV_E2E.API_KEY}, files=files)

    assert res.status_code == 200, res.content
    result = res.json()['result']
    assert len(result) == 1
    assert result[0]['face_name'] == "Marie Curie"


@pytest.mark.run(order=next(after_previous))
def test__given_five_face_img__when_recognizing_faces__then_returns_five_distinct_results():
    file = {'file': open(IMG_DIR / '000_5.jpg', 'rb')}

    res = ml_post("/recognize", headers={'X-Api-Key': ENV_E2E.API_KEY}, files=file)

    assert res.status_code == 200, res.content
    result_items = res.json()['result']
    result_items_list = [tuple(item['box'].values()) for item in result_items]
    assert itertoolz.isdistinct(result_items_list), result_items
    assert len(result_items) == 5


@pytest.mark.run(order=next(after_previous))
def test__when_getting_names__then_returns_correct_names():
    pass

    res = ml_get("/faces", headers={'X-Api-Key': ENV_E2E.API_KEY})

    result = res.json()['names']
    assert set(result) == {'Marie Curie', 'Stephen Hawking', 'Paul Walker'}


@pytest.mark.run(order=next(after_previous))
def test__given_other_api_key__when_getting_names__then_returns_no_names():
    pass

    res = ml_get("/faces", headers={'X-Api-Key': f'{ENV_E2E.API_KEY}-different'})

    result = res.json()['names']
    assert len(result) == 0


@pytest.mark.run(order=next(after_previous))
def test__when_deleting_face__then_returns_204():
    pass

    res_del = ml_delete("/faces/Paul Walker", headers={'X-Api-Key': ENV_E2E.API_KEY})

    assert res_del.status_code == 204, res_del.content
    ml_wait_until_training_is_completed(ENV_E2E.API_KEY)


# noinspection PyPep8Naming
@pytest.mark.run(order=next(after_previous))
def test__when_recognizing_faces__then_only_faces_A_and_B_are_recognized():
    files_a = {'file': open(IMG_DIR / '002_A.jpg', 'rb')}
    files_b = {'file': open(IMG_DIR / '008_B.jpg', 'rb')}
    files_c = {'file': open(IMG_DIR / '009_C.jpg', 'rb')}

    res_a = ml_post("/recognize", headers={'X-Api-Key': ENV_E2E.API_KEY}, files=files_a)
    res_b = ml_post("/recognize", headers={'X-Api-Key': ENV_E2E.API_KEY}, files=files_b)
    res_c = ml_post("/recognize", headers={'X-Api-Key': ENV_E2E.API_KEY}, files=files_c)

    assert res_a.status_code == 200, res_a.content
    result_a = res_a.json()['result']
    assert result_a[0]['face_name'] == "Marie Curie"
    assert res_b.status_code == 200, res_b.content
    result_b = res_b.json()['result']
    assert result_b[0]['face_name'] == "Stephen Hawking"
    assert res_c.status_code == 200, res_a.content
    result_c = res_c.json()['result']
    assert not (result_c[0]['face_name'] == 'Paul Walker')
    ml_wait_until_training_is_completed(ENV_E2E.API_KEY, check_response=False)


@pytest.mark.run(order=next(after_previous))
def test__when_getting_training_status__then_returns_200():
    pass

    res = ml_get("/retrain", headers={'X-Api-Key': ENV_E2E.API_KEY})

    assert res.status_code == 200, res.content


# noinspection PyPep8Naming
@pytest.mark.run(order=next(after_previous))
def test__when_deleting_face_B_with_retraining__then_returns_204():
    pass

    res = ml_delete("/faces/Stephen Hawking", headers={'X-Api-Key': ENV_E2E.API_KEY})

    assert res.status_code == 204, res.content
    ml_wait_until_training_is_completed(ENV_E2E.API_KEY, check_response=False)


@pytest.mark.run(order=next(after_previous))
def test__when_getting_training_status__then_returns_last_training_status_equals_error():
    pass

    res = ml_get("/retrain", headers={'X-Api-Key': ENV_E2E.API_KEY})

    assert res.status_code == 200, res.content
    assert res.json()['last_training_status'] == 'ERROR'


@pytest.mark.run(order=next(after_previous))
def test__when_recognizing_faces__then_returns_400_no_classifier_trained():
    files = {'file': open(IMG_DIR / '017_0.jpg', 'rb')}

    res = ml_post("/recognize", headers={'X-Api-Key': ENV_E2E.API_KEY}, files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] in [
        "400 Bad Request: "
        "No classifier model is yet trained, please train a classifier first. If the problem persists, "
        "check the amount of unique faces saved, and whether all face embeddings have been migrated to "
        f"version '{scanner}'" for scanner in ['Facenet2018', 'InsightFace']]
