from http import HTTPStatus

from src.api.constants import API_KEY_HEADER
from src.api.test.constants import VALID_API_KEY


def test__given_no_saved_faces__when_list_faces_is_requested__then_returns_empty_array(client, mocker):
    pass

    res = client.get('/faces', headers={API_KEY_HEADER: VALID_API_KEY})

    assert res.status_code == HTTPStatus.OK
    assert res.json['names'] == []
