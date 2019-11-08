from flask import Request

from src.api.exceptions import BadRequestException

UNDEFINED = '__default__'


def parse_request_bool_arg(name: str, default: bool, request: Request):
    param_value = request.args.get(name, UNDEFINED).lower()
    if param_value == UNDEFINED:
        return default
    if param_value in ('true', '1'):
        return True
    elif param_value in ('false', '0'):
        return False
    else:
        raise BadRequestException(f"'{name}' parameter accepts only 'true' (or '1') and 'false' (or '0')")
