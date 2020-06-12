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

import cv2

from src.services.imgtools.types import Array3D


class ImgScaler:
    def __init__(self, img_length_limit: int):
        self._img_length_limit = img_length_limit
        self._downscale_img_called = False
        self._downscale_coefficient = None

    def downscale_img(self, img: Array3D, interpolation=cv2.INTER_AREA) -> Array3D:
        assert not self._downscale_img_called
        self._downscale_img_called = True
        height, width = img.shape[:2]
        if width <= self._img_length_limit and height <= self._img_length_limit or not self._img_length_limit:
            return img

        self._downscale_coefficient = self._img_length_limit / (width if width >= height else height)
        new_width = round(width * self._downscale_coefficient)
        new_height = round(height * self._downscale_coefficient)
        return cv2.resize(img, dsize=(new_width, new_height), interpolation=interpolation)

    def downscale_nose(self, nose: Tuple[int, int]) -> Tuple[int, int]:
        assert self._downscale_img_called
        if not self._downscale_coefficient:
            return nose

        return nose[0] * self._downscale_coefficient, nose[1] * self._downscale_coefficient

    @property
    def downscale_coefficient(self) -> float:
        assert self._downscale_img_called
        if not self._downscale_coefficient:
            return 1

        return self._downscale_coefficient

    @property
    def upscale_coefficient(self) -> float:
        assert self._downscale_img_called
        if not self._downscale_coefficient:
            return 1

        return 1 / self._downscale_coefficient
