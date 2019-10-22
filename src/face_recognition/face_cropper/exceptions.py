from src.dto.exceptions import InvalidArgumentError


class IncorrectImageDimensionsError(InvalidArgumentError):
    pass


class NoFaceFoundError(InvalidArgumentError):
    pass
