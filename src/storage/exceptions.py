from src.dto.exceptions import IncorrectUsageError


class NoTrainedModelFoundError(IncorrectUsageError):
    message = 'No model is yet trained for this API key'
