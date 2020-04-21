from typing import Tuple

import cv2

from src.services.dto.bounding_box import BoundingBox
from src.services.imgtools.types import Array3D


class ImgScaler:
    def __init__(self, img_length_limit: int):
        self._img_length_limit = img_length_limit
        self._downscaled_img_called = False
        self._img_scale_coefficient = None

    def downscale_img(self, img: Array3D, interpolation=cv2.INTER_AREA) -> Array3D:
        assert not self._downscaled_img_called
        self._downscaled_img_called = True
        height, width = img.shape[:2]
        if width <= self._img_length_limit and height <= self._img_length_limit or not self._img_length_limit:
            return img

        self._img_scale_coefficient = self._img_length_limit / (width if width >= height else height)
        new_width = round(width * self._img_scale_coefficient)
        new_height = round(height * self._img_scale_coefficient)
        return cv2.resize(img, dsize=(new_width, new_height), interpolation=interpolation)

    @staticmethod
    def _scale_box(box: BoundingBox, scale_coefficient: float) -> BoundingBox:
        return BoundingBox(x_max=round(box.x_max * scale_coefficient),
                           x_min=round(box.x_min * scale_coefficient),
                           y_max=round(box.y_max * scale_coefficient),
                           y_min=round(box.y_min * scale_coefficient),
                           probability=box.probability)

    def upscale_box(self, box: BoundingBox) -> BoundingBox:
        assert self._downscaled_img_called
        if not self._img_scale_coefficient:
            return box
        return self._scale_box(box, 1 / self._img_scale_coefficient)

    def upscale_array(self, array):
        assert self._downscaled_img_called
        if not self._img_scale_coefficient:
            return array
        scale_coefficient = 1 / self._img_scale_coefficient
        return array * scale_coefficient

    def downscale_box(self, box: BoundingBox) -> BoundingBox:
        assert self._downscaled_img_called
        if not self._img_scale_coefficient:
            return box
        return self._scale_box(box, self._img_scale_coefficient)

    def downscale_nose(self, nose: Tuple[int, int]) -> Tuple[int, int]:
        assert self._downscaled_img_called
        if not self._img_scale_coefficient:
            return nose
        return nose[0] * self._img_scale_coefficient, nose[1] * self._img_scale_coefficient
