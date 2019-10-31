from src.exceptions import IncorrectUsageError


class NoTrainedEmbeddingClassifierFoundError(IncorrectUsageError):
    message = 'No classifier model is yet trained for this API key'


class NoEmbeddingCalculatorModelFoundError(Exception):
    message = 'No embedding calculator model is found'


class FaceHasNoEmbeddingSavedError(Exception):
    message = 'Face has no embedding saved with it'


class FileNotFound(Exception):
    message = 'File is not found in the database'
