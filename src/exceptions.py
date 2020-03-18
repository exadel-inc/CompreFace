from werkzeug.exceptions import BadRequest


class NoFileAttachedError(BadRequest):
    description = 'No file is attached'


class NoFileSelectedError(BadRequest):
    description = 'No file is selected'


class NoFaceFoundError(BadRequest):
    description = 'No faces found in the given image'


class OneDimensionalImageIsGivenError(BadRequest):
    description = 'Given image has only one dimension'
