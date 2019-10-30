import functools

from src.api.constants import API_KEY_HEADER, RETRAIN_PARAM
from src.api.exceptions import APIKeyNotSpecifiedError, NoFileAttachedError, \
    NoFileSelectedError, BadRequestException
from src.face_recognition.embedding_classifier.train import train_async


def needs_authentication(f):
    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        from flask import request

        if not request.headers.get(API_KEY_HEADER, ''):
            raise APIKeyNotSpecifiedError
        return f(*args, **kwargs)

    return wrapper


def needs_attached_file(f):
    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        from flask import request

        if 'file' not in request.files:
            raise NoFileAttachedError

        file = request.files['file']
        if file.filename == '':
            raise NoFileSelectedError

        return f(*args, **kwargs)

    return wrapper


def needs_retrain(f):
    """
    Is expected to be used only with @needs_authentication decorator,
    otherwise request.headers[API_KEY_HEADER] will throw Exception.
    """

    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        from flask import request
        retrain_param_value = request.args.get(RETRAIN_PARAM, '__default__').lower()
        if retrain_param_value in ('__default__', 'true', '1'):
            do_retrain = True
        elif retrain_param_value in ('false', '0'):
            do_retrain = False
        else:
            raise BadRequestException('Retrain parameter accepts only true and false')
        api_key = request.headers[API_KEY_HEADER]

        return_val = f(*args, **kwargs)

        if do_retrain:
            train_async(api_key)

        return return_val

    return wrapper
