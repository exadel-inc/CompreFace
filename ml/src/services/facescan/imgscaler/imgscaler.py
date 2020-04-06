import cv2

from src.services.dto.bounding_box import BoundingBox
from src.services.utils.nputils import Array3D


class ImgScaler:

    def __init__(self, size: int):
        self._size = size
        self._downscale_ratio = 0

    def downscale_img(self, img: Array3D) -> Array3D:
        h, w, c = img.shape
        bigger_dimension = h if h >= w else w
        smaller_dimension = w if w < h else h

        # only change the dimensions if they are larger than the expected size
        if bigger_dimension > self._size:
            self._downscale_ratio = bigger_dimension / self._size
            new_smaller_dimension = int(smaller_dimension / self._downscale_ratio)

            resized = cv2.resize(img, dsize=(new_smaller_dimension, self._size)) if h >= w else cv2.resize(img, dsize=(
                self._size, new_smaller_dimension))
            return resized

    def upscale_box(self, box: BoundingBox) -> BoundingBox:
        if self._downscale_ratio != 0:
            new_x_min = int(box.x_min * self._downscale_ratio)
            new_x_max = int(box.x_max * self._downscale_ratio)
            new_y_mix = int(box.y_min * self._downscale_ratio)
            new_y_max = int(box.y_max * self._downscale_ratio)
            box = BoundingBox(x_max=new_x_max, x_min=new_x_min, y_max=new_y_max, y_min=new_y_mix,
                              probability=box.probability)
        return box
