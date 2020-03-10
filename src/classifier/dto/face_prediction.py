import attr

from src._pyutils.convertible_to_dict import ConvertibleToDict
from src.facescanner.dto.bounding_box import BoundingBox


@attr.s(auto_attribs=True, frozen=True)
class FacePrediction(ConvertibleToDict):
    box: BoundingBox
    face_name: str = attr.ib(converter=str)
    probability: float = attr.ib(converter=float)
