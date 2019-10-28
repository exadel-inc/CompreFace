from src.dto.exceptions import IncorrectUsageError


class IncorrectImageDimensionsError(IncorrectUsageError):
    pass


class NoFaceFoundError(IncorrectUsageError):
    pass
