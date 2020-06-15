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

from typing import Tuple

from skimage import transform

from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.imgtools.types import Array3D


def crop_img(img: Array3D, box: BoundingBoxDTO) -> Array3D:
    return img[box.y_min:box.y_max, box.x_min:box.x_max, :]


def squish_img(img: Array3D, dimensions: Tuple[int, int]) -> Array3D:
    return transform.resize(img, dimensions)
