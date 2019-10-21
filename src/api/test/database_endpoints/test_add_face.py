from http import HTTPStatus
from unittest.mock import Mock

from src.api.constants import API_KEY_HEADER
from src.api.test.constants import VALID_API_KEY
from src.pyutils.expando import Expando


def crop_face(img):
    s = Expando()
    s.crop_face__img = img
    return s


def calc_embedding(img):
    s = Expando()
    s.calc_embedding__img = img
    return s


def test__when_add_face_is_requested__then_saves_it_correctly(client, mocker):
    img = object()
    imread_mock, get_storage_mock = mocker.Mock(return_value=img), mocker.Mock()
    imread_mock: Mock = mocker.patch('src.api.controller.imageio.imread', side_effect=imread_mock)
    mocker.patch('src.api.controller.crop_face', side_effect=crop_face)
    mocker.patch('src.api.controller.calculate_embedding', side_effect=calc_embedding)
    mocker.patch('src.api.controller.get_storage', return_value=get_storage_mock)

    res = client.post('/faces/Albert Einstein', headers={API_KEY_HEADER: VALID_API_KEY},
                      content_type='multipart/form-data',
                      data=dict(file=(b'', 'albert-einstein.jpg')))

    assert res.status_code == HTTPStatus.CREATED
    assert imread_mock.call_args[0][0].filename == 'albert-einstein.jpg'
    save_face_kwargs = get_storage_mock.add_face.call_args[1]
    assert save_face_kwargs['raw_img'] == img
    assert save_face_kwargs['face_img'].crop_face__img == img
    assert save_face_kwargs['embedding'].calc_embedding__img.crop_face__img == img
    assert save_face_kwargs['face_name'] == 'Albert Einstein'
    assert save_face_kwargs['api_key'] == VALID_API_KEY
