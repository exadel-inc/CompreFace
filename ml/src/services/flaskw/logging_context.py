import logging

from src.services.flaskw.constants import API_KEY_HEADER


class RequestContextLogFilter(logging.Filter):
    def filter(self, record):
        from flask import request
        if not request:
            return True
        infix_api_key = f" api_key={request.headers[API_KEY_HEADER]}" if API_KEY_HEADER in request.headers else ""
        record.request = (' ['
                          f'{request.method}'
                          f' {request.full_path}'
                          f'{infix_api_key}'
                          f' {request.remote_addr}'
                          f']')
        return True
