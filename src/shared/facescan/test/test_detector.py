import imageio
import pytest

from sample_images import IMG_DIR
from src.shared.facescan.backend.backend_base import BackendBase
from src.shared.facescan.exceptions import NoFaceFoundError
from src.shared.facescan.scanner import ALL_BACKENDS
from src.shared.facescan.test._scanner_cache import get_scanner
from src.shared.utils.pytestutils import raises


@pytest.mark.integration
@pytest.mark.parametrize('backend', ALL_BACKENDS)
def test__given_no_faces_img__when_scanned__then_raises_error(backend):
    scanner: BackendBase = get_scanner(backend)
    img = imageio.imread(IMG_DIR / 'no-faces.jpg')

    def act():
        scanner.scan(img)

    assert raises(NoFaceFoundError, act)


@pytest.mark.integration
@pytest.mark.parametrize('backend', ALL_BACKENDS)
def test__given_5face_jpg_img__when_scanned__then_returns_5_results(backend):
    scanner: BackendBase = get_scanner(backend)
    img = imageio.imread(IMG_DIR / 'five-faces.jpg')

    faces = scanner.scan(img)

    assert len(faces) == 5


@pytest.mark.integration
@pytest.mark.parametrize('backend', ALL_BACKENDS)
def test__given_5face_png_img__when_scanned__then_returns_5_results(backend):
    scanner: BackendBase = get_scanner(backend)
    img = imageio.imread(IMG_DIR / 'five-faces.png')[:, :, 0:3]

    faces = scanner.scan(img)

    assert len(faces) == 5


@pytest.mark.integration
@pytest.mark.parametrize('backend', ALL_BACKENDS)
def test__given_5face_img_limit3__when_scanned__then_returns_3_results(backend):
    scanner: BackendBase = get_scanner(backend)
    img = imageio.imread(IMG_DIR / 'five-faces.jpg')

    faces = scanner.scan(img, return_limit=3)

    assert len(faces) == 3
