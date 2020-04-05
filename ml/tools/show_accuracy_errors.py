import logging
import re

from sample_images.annotations import SAMPLE_IMAGES
from src.logging_ import init_logging
from src.services.facescan.scanner.facescanners import id_2_face_scanner_cls
from src.services.facescan.scanner.test.calculate_errors import calculate_errors
from src.services.utils.pyutils import get_env


def split(arr_str):
    return re.findall(r"[\w]+", arr_str)


if __name__ == '__main__':
    for scanner_id in split(get_env('SCANNERS', 'Facenet2018')):
        init_logging(level=logging.INFO)
        scanner = id_2_face_scanner_cls[scanner_id]()
        errors = calculate_errors(scanner, dataset=SAMPLE_IMAGES, show_images_with_errors=True)
        logging.info(f"Found {errors} total error(s) for {scanner.ID}")
