import numpy as np


class NPArray(np.ndarray):
    def __new__(cls, array: np.ndarray):
        if isinstance(array, NPArray):
            return array
        return array.view(cls)


Array1D = NPArray
Array3D = NPArray
