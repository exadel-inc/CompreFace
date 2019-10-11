class BadRequestException(Exception):
    pass


class APIKeyNotSpecifiedError(BadRequestException):
    def __init__(self):
        self.msg = 403

    def __str__(self):
        return str(self.msg)
