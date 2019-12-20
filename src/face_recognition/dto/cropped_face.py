import attr
from numpy.core.multiarray import ndarray

from src.face_recognition.dto.bounding_box import BoundingBox


@attr.s(auto_attribs=True, frozen=True)
class DetectedFace:
    box: BoundingBox
    is_face_prob: float


@attr.s(auto_attribs=True, frozen=True)
class CroppedFace(DetectedFace):
    img: ndarray
