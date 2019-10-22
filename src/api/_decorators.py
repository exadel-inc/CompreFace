import functools

from flask import request

from src.api.constants import API_KEY_HEADER, RETRAIN_PARAM
from src.api.exceptions import APIKeyNotSpecifiedError, APIKeyNotAuthorizedError, NoFileAttachedError, \
    NoFileSelectedError, BadRequestException
from src.api.test.constants import INVALID_API_KEY
from src.face_recognition.embedding_classifier.classifier import train_async


def needs_authentication(f):
    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        if API_KEY_HEADER not in request.headers:
            raise APIKeyNotSpecifiedError

        if request.headers[API_KEY_HEADER] == INVALID_API_KEY:
            raise APIKeyNotAuthorizedError

        return f(*args, **kwargs)

    return wrapper


def needs_attached_file(f):
    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        if 'file' not in request.files:
            raise NoFileAttachedError

        file = request.files['file']
        if file.filename == '':
            raise NoFileSelectedError

        return f(*args, **kwargs)

    return wrapper


def needs_retrain(f):
    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        if not request.args.get(RETRAIN_PARAM) or request.args.get(RETRAIN_PARAM).lower() in ('true', '1'):
            train_async(request.headers[API_KEY_HEADER])
        elif request.args.get(RETRAIN_PARAM).lower() in ('false', '0'):
            return f(*args, **kwargs)
        else:
            raise BadRequestException('Retrain parameter accepts only true and false')
        return f(*args, **kwargs)

    return wrapper
