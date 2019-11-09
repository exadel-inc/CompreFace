from src.exceptions import IncorrectUsageError


class ApiKeyNotInModels(IncorrectUsageError):
    message = 'No model is yet trained for this API key'
