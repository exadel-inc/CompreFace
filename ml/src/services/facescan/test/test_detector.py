import imageio
import pytest

from src.exceptions import NoFaceFoundError
from src.services.facescan.backend.facescan_backend import FacescanBackend
from src.services.facescan.scanner import ALL_BACKENDS
from src.services.facescan.test._scanner_cache import get_scanner
from src.services.test.sample_images import IMG_DIR
from src.services.utils.pytestutils import raises


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
def test__given_5face_jpg_img__when_scanned__then_returns_5_correct_bounding_boxes(backend, filename):
    scanner: FacescanBackend = get_scanner(backend)
    img = imageio.imread(IMG_DIR / filename)[:, :, 0:3]

    faces = scanner.scan(img)

    assert len(faces) == 5
    # TODO TASK-1


@pytest.mark.integration
@pytest.mark.parametrize('backend', ALL_BACKENDS)
def test__given_5face_img_limit3__when_scanned__then_returns_3_results(backend):
    scanner: FacescanBackend = get_scanner(backend)
    img = imageio.imread(IMG_DIR / 'five-faces.jpg')

    faces = scanner.scan(img, face_limit=3)

    assert len(faces) == 3
