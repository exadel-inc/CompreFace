import attr
from numpy.core._multiarray_umath import ndarray

from src.facescanner.dto.bounding_box import BoundingBox
from src.facescanner.dto.embedding import Embedding


@attr.s(auto_attribs=True, frozen=True)
class ScannedFace:
    box: BoundingBox
    img: ndarray
    embedding: Embedding
