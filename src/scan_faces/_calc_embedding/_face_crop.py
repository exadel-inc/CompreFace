from numpy.core.multiarray import ndarray
from skimage import transform

from src.scan_faces._calc_embedding.constants import IMAGE_SIZE
from src.scan_faces.dto.bounding_box import BoundingBox


def crop_image(img: ndarray, box: BoundingBox) -> ndarray:
    cropped_img = img[box.y_min:box.y_max, box.x_min:box.x_max, :]
    resized_img = transform.resize(cropped_img, (IMAGE_SIZE, IMAGE_SIZE))
    return resized_img
