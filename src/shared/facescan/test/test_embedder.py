import imageio
import pytest

from sample_images import IMG_DIR
from src.shared.facescan.backend.backend_base import BackendBase
from src.shared.facescan.backend.facenet import Facenet2018
from src.shared.facescan.backend.insightface.insightface import InsightFace
from src.shared.facescan.scanner import ALL_BACKENDS
from src.shared.facescan.test._scanner_cache import get_scanner
from src.shared.utils.pyutils import first_and_only

DIFFERENCE_THRESHOLD = {
    InsightFace: 400,
    Facenet2018: 0.2
}


def embeddings_are_equal(embedding1, embedding2, difference_threshold):
    difference = sum(((a - b) ** 2 for a, b in zip(embedding1, embedding2)))
    print(f"Embedding difference: {difference}, difference threshold: {difference_threshold}")
    return difference < difference_threshold


@pytest.mark.integration
@pytest.mark.parametrize('backend', ALL_BACKENDS)
def test__given_same_face_images__when_scanned__then_returns_same_embeddings(backend):
    scanner: BackendBase = get_scanner(backend)
    img1 = imageio.imread(IMG_DIR / 'personA-img1.jpg')
    img2 = imageio.imread(IMG_DIR / 'personA-img2.jpg')

    emb1 = first_and_only(scanner.scan(img1)).embedding
    emb2 = first_and_only(scanner.scan(img2)).embedding

    assert embeddings_are_equal(emb1, emb2, DIFFERENCE_THRESHOLD[backend])


@pytest.mark.integration
@pytest.mark.parametrize('backend', ALL_BACKENDS)
def test__given_diff_face_images__when_scanned__then_returns_diff_embeddings(backend):
    scanner: BackendBase = get_scanner(backend)
    img1 = imageio.imread(IMG_DIR / 'personA-img1.jpg')
    img2 = imageio.imread(IMG_DIR / 'personB-img1.jpg')

    emb1 = first_and_only(scanner.scan(img1)).embedding
    emb2 = first_and_only(scanner.scan(img2)).embedding

    assert not embeddings_are_equal(emb1, emb2, DIFFERENCE_THRESHOLD[backend])
