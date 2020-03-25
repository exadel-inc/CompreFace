import logging


class RequestContextLogFilter(logging.Filter):
    def filter(self, record):
        from flask import request
        if not request:
            return True
        record.request = (' ['
                          f'{request.method}'
                          f' {request.full_path}'
                          f' {request.remote_addr}'
                          f']')
        return True
