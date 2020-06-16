#  Copyright (c) 2020 the original author or authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

from http import HTTPStatus

import pytest

from src.exceptions import APIKeyNotSpecifiedError
from src.services.flask_.constants import API_KEY_HEADER
from src.services.flask_.needs_api_key import needs_api_key

ENDPOINT = '/endpoint'


@pytest.fixture
def client_with_locked_endpoint(app):
    @app.route(ENDPOINT)
    @needs_api_key
    def endpoint():
        return 'success-body', HTTPStatus.OK

    return app.test_client()


def test__given_no_api_key__when_requesting__then_returns_error(client_with_locked_endpoint):
    pass  # NOSONAR

    res = client_with_locked_endpoint.get(ENDPOINT)

    assert res.status_code == HTTPStatus.UNAUTHORIZED
    assert res.json['message'] == f"401 Unauthorized: {APIKeyNotSpecifiedError.description}"


def test__given_empty_api_key__when_requesting__then_returns_error(client_with_locked_endpoint):
    pass  # NOSONAR

    res = client_with_locked_endpoint.get(ENDPOINT, headers={API_KEY_HEADER: ''})

    assert res.status_code == HTTPStatus.UNAUTHORIZED
    assert res.json['message'] == f"401 Unauthorized: {APIKeyNotSpecifiedError.description}"


def test__given_valid_api_key__when_requesting__then_completes_request(client_with_locked_endpoint):
    pass  # NOSONAR

    res = client_with_locked_endpoint.get(ENDPOINT, headers={API_KEY_HEADER: 'test-api-key'})

    assert res.status_code == HTTPStatus.OK, res.json
    assert res.data.decode() == 'success-body'
