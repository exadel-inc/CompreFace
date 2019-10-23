from http import HTTPStatus

from mock import Mock
from numpy import int32, float64

from src.api.constants import API_KEY_HEADER
from src.api.test.constants import VALID_API_KEY
from src.dto import BoundingBox
from src.dto.face_prediction import FacePrediction


def test__when_recognize_endpoint_is_requested__then_returns_predictions(client, mocker):

    expected_names = [{'box': {'xmax': 10, 'xmin': 10, 'ymax': 20, 'ymin': 20}, 'prediction': 'Joe Bloggs', 'probability': 0.9}, {'box': {'xmax': 40, 'xmin': 30, 'ymax': 30, 'ymin': 40}, 'prediction': 'Fred Bloggs','probability': 0.85},
 {'box': {'xmax': 50, 'xmin': 90, 'ymax': 50, 'ymin': 50}, 'prediction': 'John Smith','probability': 0.91},
 {'box': {'xmax': 100, 'xmin': 100, 'ymax': 90, 'ymin': 100}, 'prediction': 'Albert Einstein','probability': 0.89}]

    mocker.patch('src.api.controller.imageio.imread', return_value=[])
    mocker.patch('src.api.controller.get_face_predictions', return_value=[FacePrediction(box=BoundingBox(xmin=int32(10), ymin=int32(20), xmax=int32(10), ymax=int32(20)),
                                    prediction='Joe Bloggs', probability=float64(0.9)), \
                     FacePrediction(box = BoundingBox(xmin=int32(30), ymin=int32(40), xmax=int32(40), ymax=int32(30)),
                                    prediction = 'Fred Bloggs', probability = float64(0.85)),\
                     FacePrediction(box=BoundingBox(xmin=int32(90), ymin=int32(50), xmax=int32(50), ymax=int32(50)),
                                    prediction='John Smith', probability=float64(0.91)), \
                     FacePrediction(box=BoundingBox(xmin=int32(100), ymin=int32(100), xmax=int32(100), ymax=int32(90)),
                                    prediction= 'Albert Einstein', probability=float64(0.89))])

    res = client.post('/recognize', headers={API_KEY_HEADER: VALID_API_KEY}, content_type='multipart/form-data',
                      data=dict(file=(b'', 'file')))
    assert res.status_code == HTTPStatus.OK
    assert res.json['result'] == expected_names

def test__given_no_limit_value__when_recognize_endpoint_is_requested__then_uses_no_limit(client, mocker):

    mocker.patch('src.api.controller.imageio.imread', return_value=[])
    get_face_predictions_mock: Mock = mocker.patch('src.api.controller.get_face_predictions', return_value = [])

    res = client.post('/recognize', headers={API_KEY_HEADER: VALID_API_KEY}, content_type='multipart/form-data',
                      data=dict(file=(b'', 'file')))

    assert res.status_code == HTTPStatus.OK
    assert get_face_predictions_mock.call_args[0][1] == 1

def test__given_limit_value_0__when_recognize_endpoint_is_requested__then_uses_no_limit(client, mocker):
    mocker.patch('src.api.controller.imageio.imread', return_value=[])
    get_face_predictions_mock: Mock = mocker.patch('src.api.controller.get_face_predictions', return_value = [])

    res = client.post('/recognize?limit=0', headers={API_KEY_HEADER: VALID_API_KEY}, content_type='multipart/form-data',
                      data=dict(file=(b'', 'file')))

    assert res.status_code == HTTPStatus.OK
    assert get_face_predictions_mock.call_args[0][1] == 1

def test__given_limit_value_1__when_recognize_endpoint_is_requested__then_uses_limit_1(client, mocker):
    mocker.patch('src.api.controller.imageio.imread', return_value=[])
    get_face_predictions_mock: Mock = mocker.patch('src.api.controller.get_face_predictions', return_value = [])

    res = client.post('/recognize?limit=1', headers={API_KEY_HEADER: VALID_API_KEY}, content_type='multipart/form-data',
                      data=dict(file=(b'', 'file')))

    assert res.status_code == HTTPStatus.OK
    assert get_face_predictions_mock.call_args[0][1] == 1


def test__given_limit_value_minus_1__when_recognize_endpoint_is_requested__then_returns_400(client, mocker):
    mocker.patch('src.api.controller.imageio.imread', return_value=[])
    mocker.patch('src.api.controller.get_face_predictions', return_value = [])

    res = client.post('/recognize?limit=-1', headers={API_KEY_HEADER: VALID_API_KEY}, content_type='multipart/form-data',
                      data=dict(file=(b'', 'file')))

    assert res.status_code == HTTPStatus.BAD_REQUEST
    assert res.json['message'] == 'Limit value is invalid'

def test__given_limit_value_words__when_recognize_endpoint_is_requested__then_returns_400(client, mocker):
    mocker.patch('src.api.controller.imageio.imread', return_value=[])
    mocker.patch('src.api.controller.get_face_predictions', return_value = [])

    res = client.post('/recognize?limit=-1', headers={API_KEY_HEADER: VALID_API_KEY}, content_type='multipart/form-data',
                      data=dict(file=(b'', 'file')))

    assert res.status_code == HTTPStatus.BAD_REQUEST
    assert res.json['message'] == 'Limit format is invalid'
