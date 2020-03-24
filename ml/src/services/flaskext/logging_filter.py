import logging

from src.services.flaskext.constants import API_KEY_HEADER


class ApiKeyLogFilter(logging.Filter):
    def filter(self, record):
        from flask import request
        if request and API_KEY_HEADER in request.headers:
            record.api_key = f' [api_key: {request.headers[API_KEY_HEADER]}]'
        return True
