from http import HTTPStatus

from src.api.constants import API_KEY_HEADER
from src.api.test.constants import VALID_API_KEY
FILE_BYTES = b''

def test__when_recognize_endpoint_is_requested__then_returns_predictions(client, mocker):
    filename = 'test-file.jpg'
    request_data = dict(file=(FILE_BYTES, filename))
    mocker.patch('src.api.controller.imageio.imread', return_value=[])
    mocker.patch('src.api.controller.get_face_predictions', return_value='some result')

    res = (client.post('/recognize', headers={API_KEY_HEADER: VALID_API_KEY}, content_type='multipart/form-data',
                           data=request_data))
    assert res.status_code == HTTPStatus.OK
    assert res.json['result'] == 'some result'


def test__given_no_limit_value__when_recognize_endpoint_is_requested__then_uses_no_limit(client, mocker):

    filename = 'test-file.jpg'
    request_data = dict(file=(FILE_BYTES, filename))
    expected_names = [{'box_parameters': [10, 20, 10, 20], 'prediction': 'Joe Bloggs', 'probability': 0.9},
                          {'box_parameters': [30, 40, 40, 30], 'prediction': 'Fred Bloggs', 'probability': 0.85},
                          {'box_parameters': [90, 50, 50, 50], 'prediction': 'John Smith', 'probability': 0.91},
                          {'box_parameters': [100, 100, 100, 90], 'prediction': 'Albert Einstein', 'probability': 0.89}]

    mocker.patch('src.api.controller.imageio.imread', return_value=[])
    mocker.patch('src.api.controller.get_face_predictions', return_value=[
            {'box_parameters': [10, 20, 10, 20], 'prediction': 'Joe Bloggs', 'probability': 0.9},
            {'box_parameters': [30, 40, 40, 30], 'prediction': 'Fred Bloggs', 'probability': 0.85},
            {'box_parameters': [90, 50, 50, 50], 'prediction': 'John Smith', 'probability': 0.91},
            {'box_parameters': [100, 100, 100, 90], 'prediction': 'Albert Einstein', 'probability': 0.89}])

    res = (client.post('/recognize', headers={API_KEY_HEADER: VALID_API_KEY}, content_type='multipart/form-data',
                           data=request_data))
    assert res.status_code == HTTPStatus.OK
    assert res.json['result'] == expected_names

def test__given_limit_value_0__when_recognize_endpoint_is_requested__then_uses_no_limit(client, mocker):
    filename = 'test-file.jpg'
    request_data = dict(file=(FILE_BYTES, filename), limit=0)
    expected_names = [{'box_parameters': [10, 20, 10, 20], 'prediction': 'Joe Bloggs', 'probability': 0.9},
                      {'box_parameters': [30, 40, 40, 30], 'prediction': 'Fred Bloggs', 'probability': 0.85},
                      {'box_parameters': [90, 50, 50, 50], 'prediction': 'John Smith', 'probability': 0.91},
                      {'box_parameters': [100, 100, 100, 90], 'prediction': 'Albert Einstein', 'probability': 0.89}]

    mocker.patch('src.api.controller.imageio.imread', return_value=[])
    mocker.patch('src.api.controller.get_face_predictions', return_value=[
        {'box_parameters': [10, 20, 10, 20], 'prediction': 'Joe Bloggs', 'probability': 0.9},
        {'box_parameters': [30, 40, 40, 30], 'prediction': 'Fred Bloggs', 'probability': 0.85},
        {'box_parameters': [90, 50, 50, 50], 'prediction': 'John Smith', 'probability': 0.91},
        {'box_parameters': [100, 100, 100, 90], 'prediction': 'Albert Einstein', 'probability': 0.89}])

    res = (client.post('/recognize', headers={API_KEY_HEADER: VALID_API_KEY}, content_type='multipart/form-data',
                       data=request_data))
    assert res.status_code == HTTPStatus.OK
    assert res.json['result'] == expected_names


def test__given_limit_value_1__when_recognize_endpoint_is_requested__then_uses_limit_1(client, mocker):
    filename = 'test-file.jpg'
    request_data = dict(file=(FILE_BYTES, filename), limit=1)
    expected_names = [{'box_parameters': [10, 20, 10, 20], 'prediction': 'Joe Bloggs', 'probability': 0.9}]

    mocker.patch('src.api.controller.imageio.imread', return_value=[])
    mocker.patch('src.api.controller.get_face_predictions', return_value=[
        {'box_parameters': [10, 20, 10, 20], 'prediction': 'Joe Bloggs', 'probability': 0.9}])

    res = (client.post('/recognize', headers={API_KEY_HEADER: VALID_API_KEY}, content_type='multipart/form-data',
                       data=request_data))
    assert res.status_code == HTTPStatus.OK
    assert res.json['result'] == expected_names


def test__given_limit_value_minus_1__when_recognize_endpoint_is_requested__then_returns_400(client):
    filename = 'test-file.jpg'
    request_data = dict(file=(FILE_BYTES, filename), limit=-1)
    res = (client.post('/recognize', headers={API_KEY_HEADER: VALID_API_KEY}, content_type='multipart/form-data',
                       data=request_data))
    assert res.status_code == HTTPStatus.BAD_REQUEST
    assert res.json['message'] == 'Limit value is invalid'

def test__given_limit_value_words__when_recognize_endpoint_is_requested__then_returns_400(client):
    filename = 'test-file.jpg'
    request_data = dict(file=(FILE_BYTES, filename), limit="limit")
    res = (client.post('/recognize', headers={API_KEY_HEADER: VALID_API_KEY}, content_type='multipart/form-data',
                       data=request_data))
    assert res.status_code == HTTPStatus.BAD_REQUEST
    assert res.json['message'] == 'Limit format is invalid'
