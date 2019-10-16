from http import HTTPStatus

from src.api.constants import API_KEY_HEADER
from src.api.test.constants import VALID_API_KEY, SUCCESS_BODY

FILE_BYTES = b''

def test__when_add_face_example_endpoint_is_requested__then_preprocesses_and_saves_the_face(client):
    filename = 'test-file.jpg'
    request_data = dict(file=(FILE_BYTES, filename))

    res = client.post('/faces/New Face', headers={API_KEY_HEADER: VALID_API_KEY}, content_type='multipart/form-data', data=request_data)

    assert res.status_code == HTTPStatus.OK
    assert res.data.decode() == SUCCESS_BODY

