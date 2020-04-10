import numpy as np


class NPArray(np.ndarray):
    def __new__(cls, array: np.ndarray):
        if isinstance(array, NPArray):
            return array
        return array.view(cls)


class Array1D(NPArray):
    pass


class Array3D(NPArray):
    pass
