from http import HTTPStatus

from src.api.constants import API_KEY_HEADER
from src.api.test.constants import VALID_API_KEY
FILE_BYTES = b''



def test__when_remove_face_is_requested__then_removes_the_face(client, mocker):
    mock = mocker.Mock()
    mocker.patch('src.api.controller.get_storage', return_value=mock)

    res = client.delete('/faces/John Smith', headers={API_KEY_HEADER: VALID_API_KEY})

    mock.remove_face.assert_called()

    assert res.status_code == HTTPStatus.NO_CONTENT