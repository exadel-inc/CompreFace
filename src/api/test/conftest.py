import functools
from unittest.mock import patch

import pytest

from src.api.constants import API_KEY_HEADER
from src.api.controller import create_app
from src.pyutils.pytest_utils import pass_through_decorator


def needs_authentication(f):
    """
    Makes the request.headers dictionary editable, then injects a header.
    Other attributes of request (e.g. request.url) should still work after the patch.
    """
    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        from flask import request

        class RequestMock:
            _flask_request = request
            headers = dict(request.headers)

            def __getattr__(self, name):
                return getattr(self._flask_request, name)

        with patch('src.api.controller.request', RequestMock()):
            from src.api.controller import request
            if API_KEY_HEADER not in request.headers:
                request.headers[API_KEY_HEADER] = 'valid-api-key'
            result = f(*args, **kwargs)
        return result

    return wrapper


@pytest.fixture
def app(mocker):
    mocker.patch('src.api.controller.Swagger')
    mocker.patch('src.api.controller.swag_from', pass_through_decorator)
    mocker.patch('src.api.controller.needs_authentication', needs_authentication)
    mocker.patch('src.api.controller.needs_retrain', pass_through_decorator())
    mocker.patch('src.api.controller.needs_attached_file', pass_through_decorator())
    return create_app()


@pytest.fixture
def client(app):
    return app.test_client()
