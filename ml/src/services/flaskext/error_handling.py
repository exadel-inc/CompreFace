import logging
from http import HTTPStatus

from flask import jsonify
from werkzeug.exceptions import HTTPException

from src.constants import DO_SHOW_STACKTRACE_IN_LOGS


def add_error_handling(app):
    @app.errorhandler(HTTPException)
    def handle_http_exception(e: HTTPException):
        logging.warning(str(e), exc_info=DO_SHOW_STACKTRACE_IN_LOGS)
        from flask import request
        request._logged = True
        return jsonify(message=str(e)), e.code

    @app.errorhandler(Exception)
    def handle_exception(e):
        msg = f"{e.__class__.__name__}{f': {str(e)}' if str(e) else ''}"
        logging.critical(msg, exc_info=DO_SHOW_STACKTRACE_IN_LOGS)
        from flask import request
        request._logged = True
        return jsonify(message=msg), HTTPStatus.INTERNAL_SERVER_ERROR
