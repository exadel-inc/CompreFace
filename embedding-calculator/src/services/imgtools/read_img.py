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

import imageio
import numpy as np

from src.exceptions import ImageReadLibraryError, OneDimensionalImageIsGivenError
from src.services.imgtools.types import Array3D


def _grayscale_to_rgb(img):
    """ Source: facenet library, to_rgb() function """
    w, h = img.shape
    ret = np.empty((w, h, 3), dtype=np.uint8)
    ret[:, :, 0] = ret[:, :, 1] = ret[:, :, 2] = img
    return ret


def read_img(file) -> Array3D:
    try:
        arr = imageio.imread(file)
    except (ValueError, SyntaxError) as e:
        raise ImageReadLibraryError from e

    if arr.ndim < 2:
        raise OneDimensionalImageIsGivenError
    elif arr.ndim == 2:
        arr = _grayscale_to_rgb(arr)
    else:
        arr = arr[:, :, 0:3]

    return arr
