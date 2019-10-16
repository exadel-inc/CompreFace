from typing import NamedTuple

from numpy.core.multiarray import ndarray

from src.dto import BoundingBox


class CroppedFace(NamedTuple):
    box: BoundingBox
    img: ndarray
