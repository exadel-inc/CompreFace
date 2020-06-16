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

from src.services.facescan.imgscaler.imgscaler import ImgScaler


def test__given_big_vertical_image__when_downscaling_img__then_returns_downscaled_by_height_img():
    img = np.zeros((300, 100, 3))
    scaler = ImgScaler(img_length_limit=100)

    scaled_img = scaler.downscale_img(img)

    assert scaled_img.shape == (100, 33, 3)


def test__given_big_horizontal_image__when_downscaling_img__then_returns_downscaled_by_width_img():
    img = np.zeros((100, 300, 3))
    scaler = ImgScaler(img_length_limit=100)

    scaled_img = scaler.downscale_img(img)

    assert scaled_img.shape == (33, 100, 3)


def test__given_small_image__when_downscaling_img__then_returns_same_img():
    img = np.zeros((10, 20, 3))
    scaler = ImgScaler(img_length_limit=100)

    scaled_img = scaler.downscale_img(img)

    assert (scaled_img == img).all()
