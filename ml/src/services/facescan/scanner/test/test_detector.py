import pytest

from sample_images import IMG_DIR
from sample_images.annotations import SAMPLE_IMAGES
from src.exceptions import NoFaceFoundError
from src.services.dto.bounding_box import BoundingBox
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.facescan.scanner.facescanners import ALL_SCANNERS
from src.services.facescan.scanner.test._scanner_cache import get_scanner
from src.services.facescan.scanner.test.calculate_errors import calculate_errors
from src.services.imgtools.read_img import read_img
from src.services.utils.pytestutils import raises


@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', ALL_SCANNERS)
def test__given_no_faces_img__when_scanned__then_raises_error(scanner_cls):
    scanner: FaceScanner = get_scanner(scanner_cls)
    img = read_img(IMG_DIR / 'x0.jpg')

    def act():
        scanner.scan(img)

    assert raises(NoFaceFoundError, act)


@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', ALL_SCANNERS)
@pytest.mark.parametrize('filename', ['00.x5.png', '00.x5.jpg'])
def test__given_5face_img__when_scanned__then_returns_5_correct_bounding_boxes(scanner_cls, filename):
    correct_boxes = [BoundingBox(544, 222, 661, 361, 1),
                     BoundingBox(421, 236, 530, 369, 1),
                     BoundingBox(161, 36, 266, 160, 1),
                     BoundingBox(342, 160, 437, 268, 1),
                     BoundingBox(243, 174, 352, 309, 1)]
    scanner: FaceScanner = get_scanner(scanner_cls)
    img = read_img(IMG_DIR / filename)

    faces = scanner.scan(img)

    tolerance = 20
    for face in faces:
        assert face.box.similar_to_any(correct_boxes, tolerance)


@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', ALL_SCANNERS)
def test__given_5face_img_limit3__when_scanned__then_returns_3_results(scanner_cls):
    scanner: FaceScanner = get_scanner(scanner_cls)
    img = read_img(IMG_DIR / '00.x5.jpg')

    faces = scanner.scan(img, face_limit=3)

    assert len(faces) == 3


@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', ALL_SCANNERS)
def test__given_threshold_set_to_1__when_scanned__then_returns_no_faces(scanner_cls):
    scanner: FaceScanner = get_scanner(scanner_cls)
    img = read_img(IMG_DIR / '00.x5.jpg')

    def act():
        scanner.scan(img, detection_threshold=1)

    assert raises(NoFaceFoundError, act)


@pytest.mark.skip  # TODO
@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', ALL_SCANNERS)
@pytest.mark.parametrize('row', SAMPLE_IMAGES)
def test__given_img__when_scanned__then_1_to_1_relationship_between_all_returned_boxes_and_faces(
        scanner_cls, row):
    scanner: FaceScanner = get_scanner(scanner_cls)

    errors = calculate_errors(scanner, [row])

    assert errors == 0
