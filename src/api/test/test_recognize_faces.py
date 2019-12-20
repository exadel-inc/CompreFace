from http import HTTPStatus

import pytest
from mock import Mock
from numpy import int32, float64

from src.face_recognition.dto.bounding_box import BoundingBox
from src.face_recognition.dto.face_prediction import FacePrediction
from src.pyutils.pytest_utils import one


def test__when_recognize_endpoint_is_requested__then_returns_predictions(client, mocker):
    expected_result = [
        {'box': {"xmin": 50, "ymin": 60, "xmax": 70, "ymax": 80}, 'face_name': 'Joe Bloggs',
         'probability': 0.9, 'is_face_prob': 0.9},
        {'box': {"xmin": 10, "ymin": 20, "xmax": 30, "ymax": 40}, 'face_name': 'Fred Bloggs',
         'probability': 0.85, 'is_face_prob': 0.9},
        {'box': {"xmin": 15, "ymin": 25, "xmax": 35, "ymax": 45}, 'face_name': 'John Smith',
         'probability': 0.91, 'is_face_prob': 0.9},
        {'box': {"xmin": 35, "ymin": 36, "xmax": 39, "ymax": 40}, 'face_name': 'Igor Shaw',
         'probability': 0.89, 'is_face_prob': 0.9}
    ]
    ret_val = [
        FacePrediction(box=BoundingBox(xmin=int32(50), ymin=int32(60), xmax=int32(70), ymax=int32(80)),
                       face_name='Joe Bloggs', probability=float64(0.9), is_face_prob=float64(0.9)),
        FacePrediction(box=BoundingBox(xmin=int32(10), ymin=int32(20), xmax=int32(30), ymax=int32(40)),
                       face_name='Fred Bloggs', probability=float64(0.85), is_face_prob=float64(0.9)),
        FacePrediction(box=BoundingBox(xmin=int32(15), ymin=int32(25), xmax=int32(35), ymax=int32(45)),
                       face_name='John Smith', probability=float64(0.91), is_face_prob=float64(0.9)),
        FacePrediction(box=BoundingBox(xmin=int32(35), ymin=int32(36), xmax=int32(39), ymax=int32(40)),
                       face_name='Igor Shaw', probability=float64(0.89), is_face_prob=float64(0.9))
    ]
    img = object()
    imread_mock = mocker.patch('src.api.controller.imageio.imread', return_value=img)
    get_face_predictions_mock = mocker.patch('src.api.controller.predict_from_image_with_api_key', return_value=ret_val)

    res = client.post('/recognize', data=dict(file=(b'', 'group-photo.jpg'), content_type='multipart/form-data'))

    assert res.status_code == HTTPStatus.OK, res.json
    assert one(imread_mock.call_args_list)[0][0].filename == 'group-photo.jpg'
    assert one(get_face_predictions_mock.call_args_list)[0][0] == img
    assert res.json['result'] == expected_result


@pytest.mark.parametrize("test_input,expected", [(None, 0), ("0", 0), ("1", 1)])
def test__given_limit_1_or_0_or_no_value__when_recognize_endpoint_is_required__then_uses_concrete_limit_value(client,
                                                                                                              mocker,
                                                                                                              test_input,
                                                                                                              expected):
    mocker.patch('src.api.controller.imageio.imread')
    get_face_predictions_mock: Mock = mocker.patch('src.api.controller.predict_from_image_with_api_key',
                                                   return_value=[])

    if test_input:
        res = client.post('/recognize?limit=' + test_input)
    else:
        res = client.post('/recognize')

    assert res.status_code == HTTPStatus.OK, res.json
    assert get_face_predictions_mock.call_args[0][1] == expected


def test__given_limit_value_minus_1__when_recognize_endpoint_is_requested__then_returns_400(client, mocker):
    mocker.patch('src.api.controller.imageio.imread')
    mocker.patch('src.api.controller.predict_from_image_with_api_key')

    res = client.post('/recognize?limit=-1')

    assert res.status_code == HTTPStatus.BAD_REQUEST, res.json
    assert res.json['message'] == 'Limit value is invalid'


def test__given_limit_value_words__when_recognize_endpoint_is_requested__then_returns_400(client, mocker):
    mocker.patch('src.api.controller.imageio.imread')
    mocker.patch('src.api.controller.predict_from_image_with_api_key')

    res = client.post('/recognize?limit=hello')

    assert res.status_code == HTTPStatus.BAD_REQUEST, res.json
    assert res.json['message'] == 'Limit format is invalid'
