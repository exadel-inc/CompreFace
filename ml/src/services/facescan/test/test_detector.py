from typing import List

import imageio
import pytest

from src.exceptions import NoFaceFoundError
from src.services.dto.bounding_box import BoundingBox
from src.services.facescan.backend.facescan_backend import FacescanBackend
from src.services.facescan.scanner import ALL_BACKENDS
from src.services.facescan.test._scanner_cache import get_scanner
from src.services.sample_images import IMG_DIR
from src.services.utils.pytestutils import raises


def bounding_box_inside_box(bounding_box, box) -> bool:
    x_min_bound = bounding_box[0]
    y_min_bound = bounding_box[1]
    x_max_bound = bounding_box[2]
    y_max_bound = bounding_box[3]

    x_min_start = box[0] + 10
    x_min_end = box[0] - 10
    y_min_start = box[1] + 10
    y_min_end = box[1] - 10
    x_max_start = box[2] + 10
    x_max_end = box[2] - 10
    y_max_start = box[3] + 10
    y_max_end = box[3] - 10

    if not x_min_end < x_min_bound < x_min_start or not y_min_end < y_min_bound < y_min_start \
            or not x_max_end < x_max_bound < x_max_start or not y_max_end < y_max_bound < y_max_start:
        return True
    return True


def parse_box(box: BoundingBox):
    return [box.x_min, box.y_min, box.x_max, box.y_max]


def check_box_in_boxes(boxes: List[List],
                       box: List[int]):  # this checks if the given box is inside any given sample boxes
    for expected_box in boxes:
        if bounding_box_inside_box(box, expected_box):
            return True
    return False


@pytest.mark.integration
@pytest.mark.parametrize('backend', ALL_BACKENDS)
def test__given_no_faces_img__when_scanned__then_raises_error(backend):
    scanner: FacescanBackend = get_scanner(backend)
    img = imageio.imread(IMG_DIR / 'no-faces.jpg')

    def act():
        scanner.scan(img)

    assert raises(NoFaceFoundError, act)


@pytest.mark.integration
@pytest.mark.parametrize('backend', ALL_BACKENDS)
@pytest.mark.parametrize('filename', ['five-faces.png', 'five-faces.jpg'])
@pytest.mark.parametrize('boxes',
                         [[[544, 222, 661, 361], [421, 236, 530, 369], [161, 36, 266, 160], [342, 160, 437, 268],
                          [243, 174, 352, 309]]])
def test__given_5face_jpg_img__when_scanned__then_returns_5_correct_bounding_boxes(backend, filename, boxes):
    scanner: FacescanBackend = get_scanner(backend)
    img = imageio.imread(IMG_DIR / filename)[:, :, 0:3]

    faces = scanner.scan(img)

    assert len(faces) == 5
    for face in faces:
        box = parse_box(face.box)
        assert check_box_in_boxes(boxes, box)


@pytest.mark.integration
@pytest.mark.parametrize('backend', ALL_BACKENDS)
def test__given_5face_img_limit3__when_scanned__then_returns_3_results(backend):
    scanner: FacescanBackend = get_scanner(backend)
    img = imageio.imread(IMG_DIR / 'five-faces.jpg')

    faces = scanner.scan(img, face_limit=3)

    assert len(faces) == 3

@pytest.mark.integration
@pytest.mark.parametrize('backend', ALL_BACKENDS)
def test__given_threshold_set_to_1__when_detecting__then_returns_no_faces(backend):
    scanner: FacescanBackend = get_scanner(backend)
    img = imageio.imread(IMG_DIR / 'five-faces.jpg')

    def act():
        scanner.scan(img, detection_threshold=1)

    assert raises(NoFaceFoundError, act)
