import os
from pathlib import Path

import imageio
import pytest

from main import ROOT_DIR
from src.scan_faces._calc_embedding.calculator import calculate_embedding
from src.scan_faces._calc_embedding.constants import EMBEDDING_CALCULATOR_MODEL_FILENAME
from src.scan_faces.dto.bounding_box import BoundingBox

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
CACHED_MODEL_FILEPATH = ROOT_DIR / 'models' / EMBEDDING_CALCULATOR_MODEL_FILENAME

DIFFERENCE_THRESHOLD = 0.375


def embeddings_are_the_same(embedding1, embedding2):
    return sum(((a - b) ** 2 for a, b in zip(embedding1, embedding2))) < DIFFERENCE_THRESHOLD


@pytest.mark.integration
def test_integration__when_calculating_embeddings_of_two_images_with_the_same_face__then_returns_similar_embeddings():
    person_a_im1 = imageio.imread(CURRENT_DIR / 'files' / 'personA-img1-cropped.jpg')
    person_a_im2 = imageio.imread(CURRENT_DIR / 'files' / 'personA-img2-cropped.jpg')
    img_box = BoundingBox(x_min=0, x_max=160, y_min=0, y_max=160, probability=0.95)

    person_a_face_embedding1 = calculate_embedding(person_a_im1, img_box)
    person_a_face_embedding2 = calculate_embedding(person_a_im2, img_box)

    assert embeddings_are_the_same(person_a_face_embedding1, person_a_face_embedding2)


@pytest.mark.integration
def test_integration__when_calculating_embeddings_of_two_images_with_different_faces__then_returns_different_embeddings():
    person_a_im = imageio.imread(CURRENT_DIR / 'files' / 'personA-img1-cropped.jpg')
    person_b_im = imageio.imread(CURRENT_DIR / 'files' / 'personB-img1-cropped.jpg')
    img_box = BoundingBox(x_min=0, x_max=160, y_min=0, y_max=160, probability=0.95)

    person_a_face_embedding = calculate_embedding(person_a_im, img_box)
    person_b_face_embedding = calculate_embedding(person_b_im, img_box)

    assert not embeddings_are_the_same(person_a_face_embedding, person_b_face_embedding)
