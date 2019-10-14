from http import HTTPStatus

import pytest

from src import app
from src.api._validation import needs_attached_file
from src.api.constants import API_KEY_HEADER
from src.api.exceptions import NoFileAttachedError, NoFileSelectedError
from src.api.test.constants import VALID_API_KEY, SUCCESS_BODY

ROUTE = '/test-file-endpoint'
FILE_BYTES = b''


@pytest.fixture(scope='module')
def client_with_file_endpoint():
    @app.route(ROUTE, methods=['POST'])
    @needs_attached_file
    def file_endpoint():
        return SUCCESS_BODY, HTTPStatus.OK

    return app.test_client()


def test__given_no_file__when_file_upload_endpoint_is_requested__then_completes_request(client_with_file_endpoint):
    pass

    res = client_with_file_endpoint.post(ROUTE, headers={API_KEY_HEADER: VALID_API_KEY})

    assert res.status_code == NoFileAttachedError.http_status
    assert res.json['message'] == NoFileAttachedError.message


def test__given_no_selected_file__when_file_upload_endpoint_is_requested__then_returns_error(client_with_file_endpoint):
    filename = ''
    request_data = dict(file=(FILE_BYTES, filename))

    res = client_with_file_endpoint.post(ROUTE, headers={API_KEY_HEADER: VALID_API_KEY}, data=request_data)

    assert res.status_code == NoFileSelectedError.http_status
    assert res.json['message'] == NoFileSelectedError.message


def test__given_file__when_file_upload_endpoint_is_requested__then_completes_request(client_with_file_endpoint):
    filename = 'test-file.xyz'
    request_data = dict(file=(FILE_BYTES, filename))

    res = client_with_file_endpoint.post(ROUTE, headers={API_KEY_HEADER: VALID_API_KEY}, data=request_data)

    assert res.status_code == HTTPStatus.OK
    assert res.data.decode() == SUCCESS_BODY
