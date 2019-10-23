from http import HTTPStatus

from src.api.constants import API_KEY_HEADER

FILE_BYTES = b''


def test__when_remove_face_is_requested__then_removes_the_face(client, mocker):
    get_storage_mock = mocker.Mock()
    mocker.patch('src.api.controller.get_storage', return_value=get_storage_mock)

    res = client.delete('/faces/John Smith', headers={API_KEY_HEADER: 'valid-api-key'})

    get_storage_mock.remove_face.assert_called_once_with('valid-api-key', "John Smith")
    assert res.status_code == HTTPStatus.NO_CONTENT
