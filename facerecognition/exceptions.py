from http import HTTPStatus


class FaceRecognitionAPIException(Exception):
    def __init__(self, message):
        self.http_status = HTTPStatus.INTERNAL_SERVER_ERROR
        self.message = message

    def __str__(self):
        return self.message


class BadRequestException(FaceRecognitionAPIException):
    def __init__(self, message=None):
        self.http_status = HTTPStatus.BAD_REQUEST
        self.message = message or 'Incorrect request format'


class NoFileAttachedError(BadRequestException):
    def __init__(self):
        self.message = 'No file is attached'


class NoFileSelectedError(BadRequestException):
    def __init__(self):
        self.message = 'No file is selected'


class APIKeyNotSpecifiedError(BadRequestException):
    def __init__(self):
        self.http_status = HTTPStatus.UNAUTHORIZED
        self.message = 'No API Key is given'


class APIKeyNotAuthorizedError(BadRequestException):
    def __init__(self):
        self.http_status = HTTPStatus.UNAUTHORIZED
        self.message = 'Given API Key is not authorized'
