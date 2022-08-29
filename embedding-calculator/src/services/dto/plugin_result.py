import attr
from typing import Tuple, List, Optional, Dict

from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.dto.json_encodable import JSONEncodable
from src.services.imgtools.types import Array1D, Array3D


@attr.s(auto_attribs=True, frozen=True)
class EmbeddingDTO(JSONEncodable):
    embedding: Array1D


class GenderDTO(JSONEncodable):
    def __init__(self, gender, gender_probability=1.):
        self.gender = {
            'value': gender,
            'probability': float(gender_probability)
        }


class AgeDTO(JSONEncodable):
    def __init__(self, age, age_probability=1.):
        self.age = {
            'low': age[0],
            'high': age[1],
            'probability': float(age_probability)}


class MaskDTO(JSONEncodable):
    def __init__(self, mask, mask_probability=1.):
        self.mask = {
            'value': mask,
            'probability': float(mask_probability)
        }


class PoseDTO(JSONEncodable):
    def __init__(self, pitch, yaw, roll):
        self.pose = {
            'pitch': pitch,
            'yaw': yaw,
            'roll': roll
        }


@attr.s(auto_attribs=True, frozen=True)
class LandmarksDTO(JSONEncodable):
    """ 5-points facial landmarks: eyes, nose, mouth """
    landmarks: List[Tuple[int, int]]
    NOSE_POSITION = 2

    @property
    def nose(self):
        return self.landmarks[self.NOSE_POSITION]


@attr.s(auto_attribs=True)
class FaceDTO(JSONEncodable):
    box: BoundingBoxDTO
    _img: Optional[Array3D]
    _face_img: Optional[Array3D]
    _plugins_dto: List[JSONEncodable] = attr.Factory(list)
    execution_time: Dict[str, float] = attr.Factory(dict)

    def to_json(self):
        data = super().to_json()
        for plugin_dto in self._plugins_dto:
            data.update(plugin_dto.to_json())
        return data

    @property
    def embedding(self):
        for dto in self._plugins_dto:
            if isinstance(dto, EmbeddingDTO):
                return dto.embedding

    @classmethod
    def from_request(cls, result):
        return FaceDTO(box=BoundingBoxDTO(**result['box']),
                       plugins_dto=[EmbeddingDTO(embedding=result['embedding'])],
                       execution_time=result['execution_time'],
                       face_img=None, img=None)
