import attr

from src.dto import BoundingBox
from src.dto.serializable import Serializable


@attr.s(auto_attribs=True)
class FacePrediction(Serializable):
    box: BoundingBox
    face_name: str = attr.ib(converter=str)
    probability: float = attr.ib(converter=float)
