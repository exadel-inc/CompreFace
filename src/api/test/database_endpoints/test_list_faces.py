from http import HTTPStatus

from src.api.constants import API_KEY_HEADER
from src.api.test.constants import VALID_API_KEY


def return_value_for_mock(mocker, val):
    mock = mocker.Mock()
    mock.get_all_face_names.return_value = val
    return mock


def test__given_no_saved_faces__when_list_faces_is_requested__then_returns_empty_array(client, mocker):
    expected_names = []
    mocker.patch('src.api.controller.get_storage', return_value=return_value_for_mock(mocker, expected_names))

    res = client.get('/faces', headers={API_KEY_HEADER: VALID_API_KEY})

    assert res.status_code == HTTPStatus.OK
    assert res.json['names'] == expected_names


def test__given_saved_faces__when_list_faces_is_requested__then_returns_array_with_names(client, mocker):
    expected_names = ['Joe Bloggs', 'Fred Bloggs']
    mocker.patch('src.api.controller.get_storage', return_value=return_value_for_mock(mocker, expected_names))

    res = client.get('/faces', headers={API_KEY_HEADER: VALID_API_KEY})

    assert res.status_code == HTTPStatus.OK
    assert res.json['names'] == expected_names
