import os
from pathlib import Path

import imageio
import numpy as np
import pytest

from src import pyutils
from src.face_recognition.embedding_calculator.calculator import calculate_embedding
from src.storage._mongo_database import CURRENT_CALCULATOR_MODEL_NAME

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))

CACHED_MODEL_NAME = CURRENT_CALCULATOR_MODEL_NAME

CACHED_MODEL_FILEPATH = CURRENT_DIR / 'files' / 'cache' / CACHED_MODEL_NAME
SKIP_REASON_NO_FILE_FOUND = f"Cannot calculate embeddings without model saved at {CACHED_MODEL_FILEPATH}. " \
                            f"You can generate it by running _tool_generate_cache.py while connected to the DB."

DIFFERENCE_THRESHOLD = 0.375


def embeddings_are_the_same(embedding1, embedding2):
    return np.linalg.norm(embedding1 - embedding2) < DIFFERENCE_THRESHOLD


@pyutils.run_once
def get_cached_file_contents():
    with CACHED_MODEL_FILEPATH.open('rb') as f:
        return f.read()


def return_value_for_mock(mocker, val):
    mock = mocker.Mock()
    mock.get_embedding_calculator_model.return_value = val
    return mock


@pytest.mark.integration
@pytest.mark.skipif(not CACHED_MODEL_FILEPATH.exists(), reason=SKIP_REASON_NO_FILE_FOUND)
def test_integration__when_calculating_embeddings_of_two_images_with_the_same_face__then_returns_similar_embeddings(
        mocker):
    mocker.patch('src.face_recognition.embedding_calculator.calculator.get_database',
                 return_value=return_value_for_mock(mocker, get_cached_file_contents()))
    person_a_im1 = imageio.imread(CURRENT_DIR / 'files' / 'personA-img1-cropped.jpg')
    person_a_im2 = imageio.imread(CURRENT_DIR / 'files' / 'personA-img2-cropped.jpg')

    person_a_face_embedding1 = calculate_embedding(person_a_im1)
    person_a_face_embedding2 = calculate_embedding(person_a_im2)

    assert embeddings_are_the_same(person_a_face_embedding1, person_a_face_embedding2)


@pytest.mark.integration
@pytest.mark.skipif(not CACHED_MODEL_FILEPATH.exists(), reason=SKIP_REASON_NO_FILE_FOUND)
def test_integration__when_calculating_embeddings_of_two_images_with_different_faces__then_returns_different_embeddings(
        mocker):
    mocker.patch('src.face_recognition.embedding_calculator.calculator.get_database',
                 return_value=return_value_for_mock(mocker, get_cached_file_contents()))
    person_a_im = imageio.imread(CURRENT_DIR / 'files' / 'personA-img1-cropped.jpg')
    person_b_im = imageio.imread(CURRENT_DIR / 'files' / 'personB-img1-cropped.jpg')

    person_a_face_embedding = calculate_embedding(person_a_im)
    person_b_face_embedding = calculate_embedding(person_b_im)

    assert not embeddings_are_the_same(person_a_face_embedding, person_b_face_embedding)
