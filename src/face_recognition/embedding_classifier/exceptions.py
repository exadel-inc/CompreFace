from src.dto.exceptions import IncorrectUsageError


class ApiKeyNotInModels(IncorrectUsageError):
    IncorrectUsageError.message = 'No model is yet trained for this api key'