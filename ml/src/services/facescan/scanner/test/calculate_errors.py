import logging
from typing import List
from typing import Tuple

from sample_images import IMG_DIR
from sample_images.annotations import Row
from src.services.dto.bounding_box import BoundingBox
from src.services.dto.scanned_face import ScannedFace
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.imgtools.read_img import read_img


def _calculate_errors(boxes: List[BoundingBox], noses: List[Tuple[int, int]]):
    """
    >>> _calculate_errors([BoundingBox(100,500,200,600,1)], [(150,550)])
    0
    >>> _calculate_errors([BoundingBox(100,500,200,600,1), BoundingBox(100,500,200,600,1)], [(150,550)])
    1
    >>> _calculate_errors([BoundingBox(100,500,200,600,1)], [(150,550),(150,550)])
    1
    >>> _calculate_errors([BoundingBox(100,500,200,600,1)], [(1150,1550)])
    2
    >>> _calculate_errors([BoundingBox(100,500,200,600,1)], [])
    1
    >>> _calculate_errors([], [(150,550)])
    1
    """
    missed_noses = sum(1 != sum(box.is_point_inside(nose) for box in boxes) for nose in noses)
    missed_boxes = sum(1 != sum(box.is_point_inside(nose) for nose in noses) for box in boxes)
    return missed_noses + missed_boxes


def calculate_errors(scanner: FaceScanner, dataset: List[Row], show_images_with_errors=False):
    total_errors = 0
    for row in dataset:
        img = read_img(IMG_DIR / row.image_name)
        scanned_faces = scanner.scan(img)
        boxes = [face.box for face in scanned_faces]
        errors = _calculate_errors(boxes, row.noses)
        if errors:
            logging.warning(f"Found {errors} error(s) in {row.image_name} for {scanner.ID}")
            if show_images_with_errors:
                ScannedFace.show(scanned_faces)
            total_errors += errors
    return total_errors
