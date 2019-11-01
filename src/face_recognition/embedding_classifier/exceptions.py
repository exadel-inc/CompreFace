from src.dto.exceptions import IncorrectUsageError


class ApiKeyNotInModels(IncorrectUsageError):
    message = 'No model is yet trained for this API key'

class ClassifierIsAlreadyTrainingError(IncorrectUsageError):
    message = 'API key is already training'
