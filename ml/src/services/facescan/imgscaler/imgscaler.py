from collections import namedtuple

import cv2

from src.services.dto.bounding_box import BoundingBox
from src.services.imgtools.types import Array3D


class ImgScaler:
    def __init__(self, img_length_limit: int):
        self._img_length_limit = img_length_limit
        self._downscaled_img_called = False
        self._img_downscale_ratio = False

    def downscale_img(self, img: Array3D, interpolation=cv2.INTER_AREA) -> Array3D:
        assert not self._downscaled_img_called
        self._downscaled_img_called = True
        width, height = img.shape[:2]
        if width <= self._img_length_limit and height <= self._img_length_limit:
            return img

        if width >= height:
            self._img_downscale_ratio = self._img_length_limit / width
            new_width, new_height = round(width * self._img_downscale_ratio), height
        else:
            self._img_downscale_ratio = self._img_length_limit / height
            new_width, new_height = width, round(height * self._img_downscale_ratio)
        return cv2.resize(img, dsize=(new_width, new_height), interpolation=interpolation)

    def upscale_box(self, box: BoundingBox) -> BoundingBox:
        assert self._downscaled_img_called
        if not self._img_downscale_ratio:
            return box
        box_upscale_ratio = 1 / self._img_downscale_ratio
        return BoundingBox(x_max=round(box.x_max * box_upscale_ratio),
                           x_min=round(box.x_min * box_upscale_ratio),
                           y_max=round(box.y_max * box_upscale_ratio),
                           y_min=round(box.y_min * box_upscale_ratio),
                           probability=box.probability)
