import imageio
import numpy as np

from src.shared.facescan.exceptions import IncorrectImageDimensionsError
from src.shared.facescan.types import Img


def _grayscale_to_rgb(img):
    """ Source: facenet library, to_rgb() function """
    w, h = img.shape
    ret = np.empty((w, h, 3), dtype=np.uint8)
    ret[:, :, 0] = ret[:, :, 1] = ret[:, :, 2] = img
    return ret


def read_img(file) -> Img:
    img = imageio.imread(file)

    if img.ndim < 2:
        raise IncorrectImageDimensionsError("Given image has only one dimension")
    elif img.ndim == 2:
        img = _grayscale_to_rgb(img)
    elif img.ndim > 3:
        img = img[:, :, 0:3]

    return img
