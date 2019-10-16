class FaceRecognitionCoreError(Exception):
    pass


class FaceRecognitionInternalError(FaceRecognitionCoreError):
    pass


class ThereIsNoModelForAPIKeyError(FaceRecognitionInternalError):
    pass


class FaceRecognitionInputError(FaceRecognitionCoreError):
    pass


