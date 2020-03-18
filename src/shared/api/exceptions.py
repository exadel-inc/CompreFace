from http import HTTPStatus


class APIException(Exception):
    http_status = HTTPStatus.INTERNAL_SERVER_ERROR
    message = "Internal error has occurred"

    def __str__(self):
        return self.message

    def __init__(self, message=None, *args):
        self.message = message or self.message
        super().__init__(message, message, *args)


class BadRequestException(APIException):
    http_status = HTTPStatus.BAD_REQUEST
    message = 'Bad request is provided'


class NoFileAttachedError(BadRequestException):
    message = 'No file is attached'


class NoFileSelectedError(BadRequestException):
    message = 'No file is selected'
