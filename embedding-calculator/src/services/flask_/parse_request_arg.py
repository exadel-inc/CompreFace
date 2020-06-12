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

from flask import Request

from src.exceptions import InvalidRequestArgumentValueError

UNDEFINED = '__UNDEFINED__'


def parse_request_bool_arg(name: str, default: bool, request: Request) -> bool:
    param_value = request.args.get(name.lower(), UNDEFINED).upper()
    if param_value == UNDEFINED:
        return default
    if param_value in ('TRUE', '1'):
        return True
    elif param_value in ('FALSE', '0'):
        return False
    else:
        raise InvalidRequestArgumentValueError(f"'{name}' parameter accepts only 'true' (or '1') and 'false' (or '0')")


def parse_request_string_arg(name: str, default, allowed_values, request: Request) -> str:
    name = name.lower()
    param_value = request.args.get(name.lower(), UNDEFINED).upper()
    if param_value == UNDEFINED:
        return default

    allowed_values = list(allowed_values)
    if param_value not in allowed_values:
        raise InvalidRequestArgumentValueError(f"'{name}' parameter accepts only '{', '.join(allowed_values)}' values")

    return param_value
