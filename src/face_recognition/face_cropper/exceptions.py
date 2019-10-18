class FaceCropException(Exception):
    pass


class OneDimensionalImageIsGivenError(FaceCropException):
    pass


class NoFaceFoundError(FaceCropException):
    pass
