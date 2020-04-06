from typing import Tuple

from skimage import transform

from src.services.dto.bounding_box import BoundingBox
from src.services.imgtools.types import Array3D


def crop_img(img: Array3D, box: BoundingBox) -> Array3D:
    return img[box.y_min:box.y_max, box.x_min:box.x_max, :]


def squish_img(img: Array3D, dimensions: Tuple[int, int]) -> Array3D:
    return transform.resize(img, dimensions)
