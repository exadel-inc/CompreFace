from src.exceptions import IncorrectUsageError


class NoTrainedEmbeddingClassifierFoundError(IncorrectUsageError):
    message = 'No classifier model is yet trained for this API key'


class FaceHasNoEmbeddingSavedError(Exception):
    message = 'Face has no embedding saved with it'


class NoFileFoundInDatabaseError(Exception):
    message = 'File is not found in the database'
