from src.dto.exceptions import InvalidArgumentError


class OneDimensionalImageIsGivenError(InvalidArgumentError):
    pass


class NoFaceFoundError(InvalidArgumentError):
    pass
