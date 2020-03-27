import functools

from src.exceptions import APIKeyNotSpecifiedError
from src.services.flaskext.constants import API_KEY_HEADER


def needs_authentication(f):
    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        from flask import request

        if not request.headers.get(API_KEY_HEADER, ''):
            raise APIKeyNotSpecifiedError
        return f(*args, **kwargs)

    return wrapper
