import logging

from src.services.flask_.constants import API_KEY_HEADER


class RequestContextLogFilter(logging.Filter):
    def filter(self, record):
        from flask import request
        if not request:
            return True
        infix_filename = f" file='{request.files['file'].filename}'" if 'file' in request.files else ""
        infix_api_key = f" api_key='{request.headers[API_KEY_HEADER]}'" if API_KEY_HEADER in request.headers else ""
        request_path = request.full_path[:-1] if request.full_path.endswith("?") else request.full_path
        record.request = (f'{request.method}'
                          f' {request_path}'
                          f'{infix_filename}'
                          f'{infix_api_key}'
                          f' {request.remote_addr}')
        return True
