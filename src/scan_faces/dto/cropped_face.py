import attr

from src.pyutils.convertible_to_dict import ConvertibleToDict
from src.scan_faces.dto.bounding_box import BoundingBox
from src.scan_faces.dto.embedding import Embedding


@attr.s(auto_attribs=True, frozen=True)
class DetectedFace(ConvertibleToDict):
    box: BoundingBox
    probability: float


@attr.s(auto_attribs=True, frozen=True)
class ScannedFace(DetectedFace):
    embedding: Embedding
