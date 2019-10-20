import attr
from numpy.core.multiarray import ndarray

from src.dto import BoundingBox
from src.dto.serializable import Serializable


@attr.s(auto_attribs=True)
class CroppedFace(Serializable):
    box: BoundingBox
    img: ndarray
