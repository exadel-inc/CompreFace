import functools

from src.api.constants import API_KEY_HEADER, RETRAIN_PARAM
from src.api.controller import flask_request
from src.api.exceptions import APIKeyNotSpecifiedError, APIKeyNotAuthorizedError, NoFileAttachedError, \
    NoFileSelectedError
from src.face_recognition.embedding_classifier.classifier import train_async


def needs_authentication(f):
    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        if API_KEY_HEADER not in flask_request.headers:
            raise APIKeyNotSpecifiedError

        if flask_request.headers[API_KEY_HEADER] == 'invalid-api-key':
            raise APIKeyNotAuthorizedError

        return f(*args, **kwargs)

    return wrapper


def needs_attached_file(f):
    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        if 'file' not in flask_request.files:
            raise NoFileAttachedError

        file = flask_request.files['file']
        if file.filename == '':
            raise NoFileSelectedError

        return f(*args, **kwargs)

    return wrapper


def needs_retrain(f):
    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        if not flask_request.args.get(RETRAIN_PARAM) or flask_request.args.get(RETRAIN_PARAM).lower() in ('true', '1'):
            train_async(flask_request.headers[API_KEY_HEADER])
        return f(*args, **kwargs)

    return wrapper
