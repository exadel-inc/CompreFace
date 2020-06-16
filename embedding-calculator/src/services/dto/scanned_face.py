#  Copyright (c) 2020 the original author or authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

from typing import Union

import attr

from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.dto.json_encodable import JSONEncodable
from src.services.imgtools.proc_img import crop_img
from src.services.imgtools.types import Array1D, Array3D


@attr.s(auto_attribs=True, frozen=True)
class ScannedFaceDTO(JSONEncodable):
    box: BoundingBoxDTO
    embedding: Array1D


class ScannedFace(JSONEncodable):
    def __init__(self, box: BoundingBoxDTO, embedding: Array1D, img: Union[Array3D, None], face_img: Array3D = None):
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
        return ScannedFace(box=BoundingBoxDTO(x_min=box_result['x_min'],
                                              x_max=box_result['x_max'],
                                              y_min=box_result['y_min'],
                                              y_max=box_result['y_max'],
                                              probability=box_result['probability']),
                           embedding=result['embedding'], img=None)
