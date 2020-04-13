from typing import Union

import attr

from src.services.dto.bounding_box import BoundingBox
from src.services.dto.json_encodable import JSONEncodable
from src.services.imgtools.proc_img import crop_img
from src.services.imgtools.types import Array1D, Array3D


@attr.s(auto_attribs=True, frozen=True)
class ScannedFaceDTO(JSONEncodable):
    box: BoundingBox
    embedding: Array1D


class ScannedFace(JSONEncodable):
    def __init__(self, box: BoundingBox, embedding: Array1D, img: Union[Array3D, None], face_img: Array3D = None):
        self.box = box
        self.embedding = embedding
        self.img = img
        self._face_img = face_img

    @property
    def face_img(self):
        if not self._face_img:
            self._face_img = crop_img(self.img, self.box)
        return self._face_img

    @property
    def dto(self):
        return ScannedFaceDTO(self.box, self.embedding)

    @classmethod
    def from_request(cls, result):
        box_result = result['box']
        return ScannedFace(box=BoundingBox(x_min=box_result['x_min'],
                                           x_max=box_result['x_max'],
                                           y_min=box_result['y_min'],
                                           y_max=box_result['y_max'],
                                           probability=box_result['probability']),
                           embedding=result['embedding'], img=None)
