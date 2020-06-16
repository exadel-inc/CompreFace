#  Copyright (c) 2020 the original author or authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

import logging
from http import HTTPStatus

from flask import jsonify
from werkzeug.exceptions import HTTPException

from src.constants import ENV

logger = logging.getLogger(__name__)


def add_error_handling(app):
    @app.errorhandler(HTTPException)
    def handle_http_exception(e: HTTPException):
        logging.warning(str(e), exc_info=ENV.IS_DEV_ENV)
        from flask import request
        request._logged = True
        return jsonify(message=str(e)), e.code

    @app.errorhandler(Exception)
    def handle_exception(e):
        msg = f"{e.__class__.__name__}{f': {str(e)}' if str(e) else ''}"
        logger.critical(msg, exc_info=ENV.IS_DEV_ENV)
        from flask import request
        request._logged = True
        return jsonify(message=msg), HTTPStatus.INTERNAL_SERVER_ERROR
