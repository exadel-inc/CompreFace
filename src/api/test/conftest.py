import functools
from unittest.mock import patch

import pytest
from werkzeug.datastructures import FileStorage

from src.api.constants import API_KEY_HEADER
from src.api.controller import create_app
from src.pyutils.pytest_utils import pass_through_decorator


def needs_authentication(f):
    """
    Makes the request attribute dictionary editable, then injects a key-value into it.
    Other attributes of request (e.g. request.url) should still work after the patch.
    """

    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        from flask import request

        class RequestMock:
            headers = dict(request.headers)

            def __getattr__(self, name):
                return getattr(request, name)

        request_mock = RequestMock()
        with patch('flask.request', request_mock):
            if API_KEY_HEADER not in request_mock.headers:
                request_mock.headers[API_KEY_HEADER] = 'valid-api-key'
            result = f(*args, **kwargs)
        return result

    return wrapper


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
                request_mock.files['file'] = FileStorage(filename='test-file.xyz', name='file')
            result = f(*args, **kwargs)
        return result

    return wrapper


@pytest.fixture
def app(mocker):
    mocker.patch('src.api.controller.Swagger')
    mocker.patch('src.api.controller.swag_from', pass_through_decorator)
    mocker.patch('src.api.controller.needs_authentication', needs_authentication)
    mocker.patch('src.api.controller.needs_retrain', pass_through_decorator())
    mocker.patch('src.api.controller.needs_attached_file', needs_attached_file)
    return create_app()


@pytest.fixture
def client(app):
    return app.test_client()
