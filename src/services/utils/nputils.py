from typing import Tuple

import imageio
import numpy as np
from skimage import transform

from src.dto.bounding_box import BoundingBox
from src.exceptions import OneDimensionalImageIsGivenError

Array1D = np.array
Array3D = np.array


def crop_img(img: Array3D, box: BoundingBox) -> Array3D:
    return img[box.y_min:box.y_max, box.x_min:box.x_max, :]


def squish_img(img: Array3D, dimensions: Tuple[int, int]) -> Array3D:
    return transform.resize(img, dimensions)


def _grayscale_to_rgb(img):
    """ Source: facenet library, to_rgb() function """
    w, h = img.shape
    ret = np.empty((w, h, 3), dtype=np.uint8)
    ret[:, :, 0] = ret[:, :, 1] = ret[:, :, 2] = img
    return ret


def read_img(file) -> Array3D:
    img = imageio.imread(file)

    if img.ndim < 2:
        raise OneDimensionalImageIsGivenError
    elif img.ndim == 2:
        img = _grayscale_to_rgb(img)
    else:
        img = img[:, :, 0:3]

    return img
