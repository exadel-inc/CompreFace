import logging

from flask import Response


def log_http_response(response: Response):
    from flask import request
    is_already_logged = getattr(request, '_logged', False)
    if not is_already_logged:
        logging.info(response.status.title() if response.status != '200 OK' else response.status)
    return response
