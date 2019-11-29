import attr
from numpy.core.multiarray import ndarray

from src.face_recognition.dto.bounding_box import BoundingBox
from src.pyutils.convertible_to_dict import ConvertibleToDict


@attr.s(auto_attribs=True, frozen=True)
class CroppedFace(ConvertibleToDict):
    box: BoundingBox
    img: ndarray
    is_face_prob: float
