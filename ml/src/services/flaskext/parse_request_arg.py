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
