import attr

from src.app import JSONEncodable
from src.dto.bounding_box import BoundingBox


@attr.s(auto_attribs=True, frozen=True)
class NamePrediction(JSONEncodable):
    face_name: str = attr.ib(converter=str)
    probability: float = attr.ib(converter=float)


@attr.s(auto_attribs=True, frozen=True)
class FacePrediction(NamePrediction):
    box: BoundingBox
