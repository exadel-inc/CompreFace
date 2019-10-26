"""
Variable origin checking is used in these tests.

Imagine we have three functions
    funA(x) -> y
    funB(y) -> z
    funC(z) -> q

And if we need to make sure that they are mocked all called in the right order in the following controller:
    def test_me(x) -> q:
        return funC(funB(funA(x)))

We can do by assigning "ISFROM_<fun>_GETARG_<arg>" properties to mocks, and then use them like this:
    x = ...
    q = test_me(x)
    assert x == q.ISFROM_funC_GETARG_z.ISFROM_funB_GETARG_y.ISFROM_funA_GETARG_x

Analogically, to get property origin, we can use "PROP_<name>" syntax
"""

from http import HTTPStatus

from mock import Mock

from src.pyutils.pytest_utils import Expando


def crop_face(img):
    s = Expando()
    s.ISFROM_crop_face_GETARG_img = img
    s.img = Expando()
    s.img.PROP_img = s
    return s


def calc_embedding(img):
    s = Expando()
    s.ISFROM_calc_embedding_GETARG_img = img
    return s


def test__when_add_face_is_requested__then_saves_it_correctly(client, mocker):
    img = object()
    imread_mock, get_storage_mock = mocker.Mock(return_value=img), mocker.Mock()
    imread_mock: Mock = mocker.patch('src.api.controller.imageio.imread', imread_mock)
    mocker.patch('src.api.controller.crop_face', crop_face)
    mocker.patch('src.api.controller.calculate_embedding', calc_embedding)
    mocker.patch('src.api.controller.get_storage', return_value=get_storage_mock)

    res = client.post('/faces/Albert Einstein', content_type='multipart/form-data',
                      data=dict(file=(b'', 'albert-einstein.jpg')))

    assert res.status_code == HTTPStatus.CREATED, res.json
    assert imread_mock.call_args_list[0][0][0].filename == 'albert-einstein.jpg'
    save_face_kwargs = get_storage_mock.add_face.call_args[1]
    assert save_face_kwargs['raw_img'] == img
    assert save_face_kwargs['face_img'].PROP_img.ISFROM_crop_face_GETARG_img == img
    assert save_face_kwargs['embedding'].ISFROM_calc_embedding_GETARG_img.PROP_img.ISFROM_crop_face_GETARG_img == img
    assert save_face_kwargs['face_name'] == 'Albert Einstein'
    assert save_face_kwargs['api_key'] == 'api-key-001'
