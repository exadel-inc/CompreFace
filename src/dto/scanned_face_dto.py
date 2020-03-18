import attr

from src.app import JSONEncodable
from src.dto.bounding_box import BoundingBox
from src.services.utils.nputils import Array1D


@attr.s(auto_attribs=True, frozen=True)
class ScannedFaceDTO(JSONEncodable):
    box: BoundingBox
    embedding: Array1D
