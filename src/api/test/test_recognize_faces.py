from http import HTTPStatus

import pytest
from mock import Mock
from numpy import int32, float64

from src.facescanner.dto.bounding_box import BoundingBox

from src.classifier.dto.face_prediction import FacePrediction
from src._pyutils.pytest_utils import one


def test__when_recognize_endpoint_is_requested__then_returns_predictions(client, mocker):
    expected_result = [
        {'box': {"x_min": 50, "y_min": 60, "x_max": 70, "y_max": 80, 'probability':0.9}, 'face_name': 'Joe Bloggs',
         'probability': 0.9},
        {'box': {"x_min": 10, "y_min": 20, "x_max": 30, "y_max": 40, 'probability':0.9}, 'face_name': 'Fred Bloggs',
         'probability': 0.85},
        {'box': {"x_min": 15, "y_min": 25, "x_max": 35, "y_max": 45, 'probability':0.9}, 'face_name': 'John Smith',
         'probability': 0.91},
        {'box': {"x_min": 35, "y_min": 36, "x_max": 39, "y_max": 40, 'probability':0.9}, 'face_name': 'Igor Shaw',
         'probability': 0.89}
    ]
    ret_val = [
        FacePrediction(box=BoundingBox(x_min=int32(50), y_min=int32(60), x_max=int32(70), y_max=int32(80), probability=float64(0.9)),
                       face_name='Joe Bloggs', probability=float64(0.9)),
        FacePrediction(box=BoundingBox(x_min=int32(10), y_min=int32(20), x_max=int32(30), y_max=int32(40), probability=float64(0.9)),
                       face_name='Fred Bloggs', probability=float64(0.85)),
        FacePrediction(box=BoundingBox(x_min=int32(15), y_min=int32(25), x_max=int32(35), y_max=int32(45), probability=float64(0.9)),
                       face_name='John Smith', probability=float64(0.91)),
        FacePrediction(box=BoundingBox(x_min=int32(35), y_min=int32(36), x_max=int32(39), y_max=int32(40), probability=float64(0.9)),
                       face_name='Igor Shaw', probability=float64(0.89))
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
