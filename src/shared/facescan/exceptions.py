from src.exceptions import InvalidInputError


class NoFaceFoundError(InvalidInputError):
    pass


class IncorrectImageDimensionsError(InvalidInputError):
    pass
