from http import HTTPStatus

import pytest

from src.exceptions import APIKeyNotSpecifiedError
from src.services.flaskw.constants import API_KEY_HEADER
from src.services.flaskw.needs_authentication import needs_authentication


@pytest.fixture
def client_with_locked_endpoint(app):
    @app.route('/endpoint')
    @needs_authentication
    def endpoint():
        return 'success-body', HTTPStatus.OK

    return app.test_client()


def test__given_no_api_key__when_requesting__then_returns_error(client_with_locked_endpoint):
    pass

    res = client_with_locked_endpoint.get('/endpoint')

    assert res.status_code == HTTPStatus.UNAUTHORIZED
    assert res.json['message'] == f"401 Unauthorized: {APIKeyNotSpecifiedError.description}"


def test__given_empty_api_key__when_requesting__then_returns_error(client_with_locked_endpoint):
    pass

    res = client_with_locked_endpoint.get('/endpoint', headers={API_KEY_HEADER: ''})

    assert res.status_code == HTTPStatus.UNAUTHORIZED
    assert res.json['message'] == f"401 Unauthorized: {APIKeyNotSpecifiedError.description}"


def test__given_valid_api_key__when_requesting__then_completes_request(client_with_locked_endpoint):
    pass

    res = client_with_locked_endpoint.get('/endpoint', headers={API_KEY_HEADER: 'test-api-key'})

    assert res.status_code == HTTPStatus.OK, res.json
    assert res.data.decode() == 'success-body'
