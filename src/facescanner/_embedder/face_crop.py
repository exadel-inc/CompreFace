from numpy.core.multiarray import ndarray
from skimage import transform

from src.facescanner._embedder.constants import IMAGE_SIZE
from src.facescanner.dto.bounding_box import BoundingBox
from src.facescanner.dto.cropped_img import CroppedImg


def crop_image(img: ndarray, box: BoundingBox) -> CroppedImg:
    cropped_img = img[box.y_min:box.y_max, box.x_min:box.x_max, :]
    resized_img = transform.resize(cropped_img, (IMAGE_SIZE, IMAGE_SIZE))
    return resized_img
