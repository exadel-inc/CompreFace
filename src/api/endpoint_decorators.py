import functools

from src.api.constants import API_KEY_HEADER, GET_PARAM, RETRAIN_VALUES
from src.api.exceptions import APIKeyNotSpecifiedError, NoFileAttachedError, \
    NoFileSelectedError
from src.api.parse_request_arg import parse_request_string_arg
from src.api.training_task_manager import start_training


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
        retrain_value = parse_request_string_arg(name=GET_PARAM.RETRAIN, default=RETRAIN_VALUES.FORCE,
                                                 allowed_values=RETRAIN_VALUES, request=request)
        api_key = request.headers[API_KEY_HEADER]

        return_val = f(*args, **kwargs)

        if retrain_value == RETRAIN_VALUES.YES:
            start_training(api_key)
        elif retrain_value == RETRAIN_VALUES.FORCE:
            start_training(api_key, force=True)

        return return_val

    return wrapper
