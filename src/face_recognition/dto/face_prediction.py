import attr

from src.face_recognition.dto.bounding_box import BoundingBox
from src.pyutils.convertible_to_dict import ConvertibleToDict


@attr.s(auto_attribs=True, frozen=True)
class FacePrediction(ConvertibleToDict):
    box: BoundingBox
    face_name: str = attr.ib(converter=str)
    probability: float = attr.ib(converter=float)
    is_face_prob: float = attr.ib(converter=float)
