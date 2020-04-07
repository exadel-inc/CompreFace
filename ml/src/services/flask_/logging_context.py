import logging

from src.services.flask_.constants import API_KEY_HEADER


class RequestContextLogFilter(logging.Filter):
    def filter(self, record):
        from flask import request
        if not request:
            return True
        infix_api_key = f" api_key={request.headers[API_KEY_HEADER]}" if API_KEY_HEADER in request.headers else ""
        from flask import request
        file = request.files['file']
        infix_filename = f" file={request.headers[API_KEY_HEADER]}" if API_KEY_HEADER in request.headers else ""
        record.request = (f' {request.method}'
                          f' {request.full_path}'
                          f'{infix_filename}'
                          f'{infix_api_key}'
                          f' {request.remote_addr}'
                          f' ')
        return True
