from http import HTTPStatus

import pytest

from src.api._decorators import needs_authentication
from src.api.constants import API_KEY_HEADER
from src.api.exceptions import APIKeyNotSpecifiedError, APIKeyNotAuthorizedError

ROUTE = '/test-locked-endpoint'


@pytest.fixture
def client_with_locked_endpoint(app):
    @app.route(ROUTE)
    @needs_authentication
    def locked_endpoint():
        return 'success-body', HTTPStatus.OK

    return app.test_client()


def test__given_no_api_key__when_locked_endpoint_is_requested__then_returns_error(client_with_locked_endpoint):
    pass

    res = client_with_locked_endpoint.get(ROUTE)

    assert res.status_code == APIKeyNotSpecifiedError.http_status
    assert res.json['message'] == APIKeyNotSpecifiedError.message


def test__given_invalid_api_key__when_locked_endpoint_is_requested__then_returns_error(client_with_locked_endpoint):
    pass

    res = client_with_locked_endpoint.get(ROUTE, headers={API_KEY_HEADER: 'invalid-api-key'})

    assert res.status_code == APIKeyNotAuthorizedError.http_status
    assert res.json['message'] == APIKeyNotAuthorizedError.message


def test__given_valid_api_key__when_locked_endpoint_is_requested__then_completes_request(client_with_locked_endpoint):
    pass

    res = client_with_locked_endpoint.get(ROUTE, headers={API_KEY_HEADER: 'valid-api-key'})

    assert res.status_code == HTTPStatus.OK
    assert res.data.decode() == 'success-body'
