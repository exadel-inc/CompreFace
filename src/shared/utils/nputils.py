from io import BytesIO
from typing import Tuple

import joblib
from skimage import transform

from src.shared.facescan.types import Img, BoundingBox, CroppedImg


def crop_image(img: Img, box: BoundingBox) -> CroppedImg:
    return img[box.y_min:box.y_max, box.x_min:box.x_max, :]


def squish_image(img: Img, dimensions: Tuple[int, int]) -> CroppedImg:
    return transform.resize(img, dimensions)


def serialize(obj: object) -> bytes:
    bytes_container = BytesIO()
    joblib.dump(obj, bytes_container)  # Works better with numpy arrays than pickle
    bytes_container.seek(0)  # update to enable reading
    bytes_data = bytes_container.read()
    return bytes_data


def deserialize(bytes_data: bytes) -> object:
    bytes_container = BytesIO(bytes_data)
    obj = joblib.load(bytes_container)
    return obj
