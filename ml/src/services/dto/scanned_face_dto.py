import attr

from src.services.dto.bounding_box import BoundingBox
from src.services.dto.json_encodable import JSONEncodable
from src.services.utils.nputils import Array1D


@attr.s(auto_attribs=True, frozen=True)
class ScannedFaceDTO(JSONEncodable):
    box: BoundingBox
    embedding: Array1D
