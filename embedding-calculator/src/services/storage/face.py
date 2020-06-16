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

import attr

from src.services.imgtools.types import Array1D, Array3D


@attr.s(auto_attribs=True, frozen=True, cmp=False)
class FaceNameEmbedding:
    name: str
    embedding: Array1D


@attr.s(auto_attribs=True, frozen=True, cmp=False)
class Face(FaceNameEmbedding):
    raw_img: Array3D
    face_img: Array3D
