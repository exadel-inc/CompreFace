from werkzeug.exceptions import BadRequest, Unauthorized


class APIKeyNotSpecifiedError(Unauthorized):
    description = 'No API Key is given'


class NoFileAttachedError(BadRequest):
    description = "No file is attached"


class NoFileSelectedError(BadRequest):
    description = "No file is selected"


class NoFaceFoundError(BadRequest):
    description = "No face is found in the given image"


class OneDimensionalImageIsGivenError(BadRequest):
    description = "Given image has only one dimension"


class MoreThanOneFaceFoundError(BadRequest):
    description = "Found more than one face in the given image"


class InvalidRequestArgumentValueError(BadRequest):
    description = 'Invalid request argument value is given'


class ImageReadLibraryError(BadRequest):
    description = 'Image has incorrect format or is broken'
