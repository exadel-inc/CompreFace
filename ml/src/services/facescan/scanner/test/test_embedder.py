import pytest

from sample_images import IMG_DIR
from src.services.facescan.scanner.facenet.facenet import Facenet2018
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.facescan.scanner.facescanners import ALL_SCANNERS
from src.services.facescan.scanner.insightface.insightface import InsightFace
from src.services.facescan.scanner.test._scanner_cache import get_scanner
from src.services.imgtools.read_img import read_img
from src.services.utils.pyutils import first_and_only

DIFFERENCE_THRESHOLD = {
    InsightFace: 400,
    Facenet2018: 0.2
}


def embeddings_are_equal(embedding1, embedding2, difference_threshold):
    difference = sum(((a - b) ** 2 for a, b in zip(embedding1, embedding2)))
    print(f"Embedding difference: {difference}, difference threshold: {difference_threshold}")
    return difference < difference_threshold


@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', ALL_SCANNERS)
def test__given_same_face_images__when_scanned__then_returns_same_embeddings(scanner_cls):
    scanner: FaceScanner = get_scanner(scanner_cls)
    img1 = read_img(IMG_DIR / '07.B.jpg')
    img2 = read_img(IMG_DIR / '08.B.jpg')

    emb1 = first_and_only(scanner.scan(img1)).embedding
    emb2 = first_and_only(scanner.scan(img2)).embedding

    assert embeddings_are_equal(emb1, emb2, DIFFERENCE_THRESHOLD[scanner_cls])


@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', ALL_SCANNERS)
def test__given_diff_face_images__when_scanned__then_returns_diff_embeddings(scanner_cls):
    scanner: FaceScanner = get_scanner(scanner_cls)
    img1 = read_img(IMG_DIR / '07.B.jpg')
    img2 = read_img(IMG_DIR / '09.C.jpg')

    emb1 = first_and_only(scanner.scan(img1)).embedding
    emb2 = first_and_only(scanner.scan(img2)).embedding

    assert not embeddings_are_equal(emb1, emb2, DIFFERENCE_THRESHOLD[scanner_cls])
