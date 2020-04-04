import pytest

from sample_images import IMG_DIR
from src.exceptions import NoFaceFoundError
from src.services.dto.bounding_box import BoundingBox
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.facescan.scanner.facescanners import ALL_SCANNERS
from src.services.facescan.scanner.test._scanner_cache import get_scanner
from src.services.imgtools.read_img import read_img
from src.services.utils.pytestutils import raises


@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', ALL_SCANNERS)
def test__given_no_faces_img__when_scanned__then_raises_error(scanner_cls):
    scanner: FaceScanner = get_scanner(scanner_cls)
    img = read_img(IMG_DIR / 'no-faces.jpg')

    def act():
        scanner.scan(img)

    assert raises(NoFaceFoundError, act)


@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', ALL_SCANNERS)
@pytest.mark.parametrize('filename', ['five-faces.png', 'five-faces.jpg'])
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
    img = read_img(IMG_DIR / 'five-faces.jpg')

    faces = scanner.scan(img, face_limit=3)

    assert len(faces) == 3


@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', ALL_SCANNERS)
def test__given_threshold_set_to_1__when_detecting__then_returns_no_faces(scanner_cls):
    scanner: FaceScanner = get_scanner(scanner_cls)
    img = read_img(IMG_DIR / 'five-faces.jpg')

    def act():
        scanner.scan(img, detection_threshold=1)

    assert raises(NoFaceFoundError, act)
