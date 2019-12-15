import functools
from unittest.mock import patch

import pytest

from src.api.controller import create_app




def needs_attached_file(f):
    """
    Makes the request attribute dictionary editable, then injects a key-value into it.
    Other attributes of request (e.g. request.url) should still work after the patch.
    """

    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        from flask import request

        class RequestMock:
            files = dict(request.files)

            def __getattr__(self, name):
                return getattr(request, name)

        request_mock = RequestMock()
        with patch('flask.request', request_mock):
            if 'file' not in request_mock.files:
                request_mock.files['file'] = object()
            return f(*args, **kwargs)

    return wrapper


@pytest.fixture
def app(mocker):
    mocker.patch('src.api.controller.Swagger')
    mocker.patch('src.api.controller.needs_attached_file', needs_attached_file)
    return create_app()


@pytest.fixture
def client(app):
    return app.test_client()
