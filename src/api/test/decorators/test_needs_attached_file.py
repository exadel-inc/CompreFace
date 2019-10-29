from http import HTTPStatus

import pytest

from src.api._decorators import needs_attached_file
from src.api.constants import API_KEY_HEADER
from src.api.exceptions import NoFileAttachedError, NoFileSelectedError

FILE_BYTES = b''


@pytest.fixture
def client_with_file_endpoint(app):
    @app.route('/endpoint', methods=['POST'])
    @needs_attached_file
    def endpoint():
        return 'success-body', HTTPStatus.OK

    return app.test_client()


def test__given_no_file__when_file_upload_endpoint_is_requested__then_completes_request(client_with_file_endpoint):
    pass

    res = client_with_file_endpoint.post('/endpoint', headers={API_KEY_HEADER: 'api-key-001'})

    assert res.status_code == NoFileAttachedError.http_status
    assert res.json['message'] == NoFileAttachedError.message


def test__given_no_selected_file__when_file_upload_endpoint_is_requested__then_returns_error(client_with_file_endpoint):
    filename = ''
    request_data = dict(file=(FILE_BYTES, filename))

    res = client_with_file_endpoint.post('/endpoint', headers={API_KEY_HEADER: 'api-key-001'}, data=request_data)

    assert res.status_code == NoFileSelectedError.http_status
    assert res.json['message'] == NoFileSelectedError.message


def test__given_file__when_file_upload_endpoint_is_requested__then_completes_request(client_with_file_endpoint):
    filename = 'test-file.xyz'
    request_data = dict(file=(FILE_BYTES, filename))

    res = client_with_file_endpoint.post('/endpoint', headers={API_KEY_HEADER: 'api-key-001'}, data=request_data)

    assert res.status_code == HTTPStatus.OK, res.json
    assert res.data.decode() == 'success-body'
