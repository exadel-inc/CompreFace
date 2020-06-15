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

import numpy as np

from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.facescan.imgscaler.imgscaler import ImgScaler


def test__given_downscaled_image__when_upscaling_box__then_returns_upscaled_box():
    img = np.zeros((200, 200, 3))
    scaler = ImgScaler(img_length_limit=100)
    scaler.downscale_img(img)

    output_box = BoundingBoxDTO(10, 10, 20, 20, 1).scaled(scaler.upscale_coefficient)

    assert output_box == BoundingBoxDTO(20, 20, 40, 40, 1)


def test__given_not_downscaled_image__when_upscaling_box__then_returns_same_box():
    img = np.zeros((20, 20, 3))
    scaler = ImgScaler(img_length_limit=100)
    scaler.downscale_img(img)

    output_box = BoundingBoxDTO(10, 10, 20, 20, 1).scaled(scaler.upscale_coefficient)

    assert output_box == BoundingBoxDTO(10, 10, 20, 20, 1)
