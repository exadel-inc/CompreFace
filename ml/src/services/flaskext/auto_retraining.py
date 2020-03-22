import functools

from src.services.classifier.classifier_manager import TrainingTaskManager
from src.services.flaskext.constants import GetParameter, RetrainValue, API_KEY_HEADER
from src.services.flaskext.parse_request_arg import parse_request_string_arg
from src.singletons import get_training_task_manager


def needs_retrain(f):
    """
    Is expected to be used only with @needs_authentication decorator,
    otherwise request.headers[API_KEY_HEADER] will throw Exception.
    """

    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        from flask import request
        retrain_value = parse_request_string_arg(name=GetParameter.RETRAIN, default=RetrainValue.FORCE,
                                                 allowed_values=RetrainValue, request=request)
        api_key = request.headers[API_KEY_HEADER]

        return_val = f(*args, **kwargs)

        task_manager: TrainingTaskManager = get_training_task_manager()
        if retrain_value == RetrainValue.YES:
            task_manager.start_training(api_key)
        elif retrain_value == RetrainValue.FORCE:
            task_manager.start_training(api_key, force=True)

        return return_val

    return wrapper
