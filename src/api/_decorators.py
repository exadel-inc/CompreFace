import functools

from src.api.constants import API_KEY_HEADER, RETRAIN_PARAM
from src.api.exceptions import APIKeyNotSpecifiedError, NoFileAttachedError, \
    NoFileSelectedError, BadRequestException
from src.face_recognition.embedding_classifier.classifier import train_async


def needs_authentication(f):
    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        from flask import request

        if API_KEY_HEADER not in request.headers or not request.headers[API_KEY_HEADER]:
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
        retrain_param_value = request.args.get(RETRAIN_PARAM, '').lower()
        if retrain_param_value in ('true', '1'):
            do_retrain = True
        elif retrain_param_value in ('false', '0'):
            do_retrain = False
        else:
            raise BadRequestException('Retrain parameter accepts only true and false')
        api_key = request.headers[API_KEY_HEADER]

        return_val = f(*args, **kwargs)

        if do_retrain:
            train_thread = train_async(api_key)
            # TODO EGP-708 Remove this temporary 'await' parameter once there is an official way for E2E tests to wait for the training to finish
            if request.args.get('await', '').lower() in ('true', '1'):
                train_thread.join()

        return return_val

    return wrapper
