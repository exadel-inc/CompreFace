import imageio
import numpy as np

from src.exceptions import ImageReadLibraryError, OneDimensionalImageIsGivenError
from src.services.imgtools.types import Array3D
from src.services.storage._serialization import deserialize


def _grayscale_to_rgb(img):
    """ Source: facenet library, to_rgb() function """
    w, h = img.shape
    ret = np.empty((w, h, 3), dtype=np.uint8)
    ret[:, :, 0] = ret[:, :, 1] = ret[:, :, 2] = img
    return ret


def read_img(file) -> Array3D:
    img_source = file.read() if hasattr(file, 'read') else file  # TODO EFRS-517 Remove this line
    try:
        arr = deserialize(img_source)
        assert isinstance(arr, Array3D)
        return arr  # TODO EFRS-517 Remove this line
    except:  # NOQA
        pass

    try:
        arr = imageio.imread(img_source)
    except (ValueError, SyntaxError) as e:
        raise ImageReadLibraryError from e

    if arr.ndim < 2:
        raise OneDimensionalImageIsGivenError
    elif arr.ndim == 2:
        arr = _grayscale_to_rgb(arr)
    else:
        arr = arr[:, :, 0:3]

    return arr
