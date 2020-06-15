#  Copyright (c) 2020 the original author or authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

import functools

from src.cache import get_training_task_manager
from src.services.async_task_manager.async_task_manager import TrainingTaskManagerBase
from src.services.flask_.constants import GetParameter, RetrainValue, API_KEY_HEADER
from src.services.flask_.parse_request_arg import parse_request_string_arg


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

        task_manager: TrainingTaskManagerBase = get_training_task_manager()
        if retrain_value == RetrainValue.NO:
            pass  # Skip retraining
        elif retrain_value == RetrainValue.YES:
            task_manager.start_training(api_key)
        elif retrain_value == RetrainValue.FORCE:
            task_manager.start_training(api_key, force=True)
        else:
            raise ValueError
        return return_val

    return wrapper
