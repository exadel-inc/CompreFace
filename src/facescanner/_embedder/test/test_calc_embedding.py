import os
from pathlib import Path

import imageio
import pytest

from src.facescanner._embedder.embedder import calculate_embedding
from src.facescanner.dto.bounding_box import BoundingBox

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
DIFFERENCE_THRESHOLD = 0.1


def embeddings_are_the_same(embedding1, embedding2):
    return sum(((a - b) ** 2 for a, b in zip(embedding1, embedding2))) < DIFFERENCE_THRESHOLD


@pytest.mark.integration
def test_integration__when_calculating_embeddings_of_two_images_with_the_same_face__then_returns_similar_embeddings():
    person_a_im1 = imageio.imread(CURRENT_DIR / '_files' / 'personA-img1-cropped.jpg')
    person_a_im2 = imageio.imread(CURRENT_DIR / '_files' / 'personA-img2-cropped.jpg')

    person_a_face_embedding1 = calculate_embedding(person_a_im1)
    person_a_face_embedding2 = calculate_embedding(person_a_im2)

    assert embeddings_are_the_same(person_a_face_embedding1, person_a_face_embedding2)


@pytest.mark.integration
def test_integration__when_calculating_embeddings_of_two_images_with_different_faces__then_returns_different_embeddings():
    person_a_im = imageio.imread(CURRENT_DIR / '_files' / 'personA-img1-cropped.jpg')
    person_b_im = imageio.imread(CURRENT_DIR / '_files' / 'personB-img1-cropped.jpg')

    person_a_face_embedding = calculate_embedding(person_a_im)
    person_b_face_embedding = calculate_embedding(person_b_im)

    assert not embeddings_are_the_same(person_a_face_embedding, person_b_face_embedding)
