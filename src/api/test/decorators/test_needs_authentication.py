from http import HTTPStatus

import pytest

from src.api._decorators import needs_authentication
from src.api.controller import app
from src.api.constants import API_KEY_HEADER
from src.api.exceptions import APIKeyNotSpecifiedError, APIKeyNotAuthorizedError
from src.api.test.constants import INVALID_API_KEY, SUCCESS_BODY, VALID_API_KEY

ROUTE = '/test-locked-endpoint'


@pytest.fixture(scope='module')
def client_with_locked_endpoint():
    @app.route(ROUTE)
    @needs_authentication
    def locked_endpoint():
        return SUCCESS_BODY, HTTPStatus.OK

    return app.test_client()


def test__given_no_api_key__when_locked_endpoint_is_requested__then_returns_error(client_with_locked_endpoint):
    pass

    res = client_with_locked_endpoint.get(ROUTE)

    assert res.status_code == APIKeyNotSpecifiedError.http_status
    assert res.json['message'] == APIKeyNotSpecifiedError.message


def test__given_invalid_api_key__when_locked_endpoint_is_requested__then_returns_error(client_with_locked_endpoint):
    pass

    res = client_with_locked_endpoint.get(ROUTE, headers={API_KEY_HEADER: INVALID_API_KEY})

    assert res.status_code == APIKeyNotAuthorizedError.http_status
    assert res.json['message'] == APIKeyNotAuthorizedError.message


def test__given_valid_api_key__when_locked_endpoint_is_requested__then_completes_request(client_with_locked_endpoint):
    pass

    res = client_with_locked_endpoint.get(ROUTE, headers={API_KEY_HEADER: VALID_API_KEY})

    assert res.status_code == HTTPStatus.OK
    assert res.data.decode() == SUCCESS_BODY
