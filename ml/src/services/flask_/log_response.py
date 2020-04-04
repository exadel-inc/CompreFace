import logging

from flask import Response


def log_http_response(response: Response):
    from flask import request
    if not getattr(request, '_logged', False):
        logging.info(response.status.title() if response.status != '200 OK' else response.status)
    return response
