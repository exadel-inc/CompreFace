import os
from pathlib import Path

import imageio
import numpy as np
import pytest

from main import ROOT_DIR
from src import pyutils
from src.scan_faces._calc_embedding.calculator import calculate_embedding
from src.scan_faces._calc_embedding.constants import EMBEDDING_CALCULATOR_MODEL_FILENAME

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
CACHED_MODEL_FILEPATH = ROOT_DIR / 'models' / EMBEDDING_CALCULATOR_MODEL_FILENAME

DIFFERENCE_THRESHOLD = 0.375


def embeddings_are_the_same(embedding1, embedding2):
    return np.linalg.norm(embedding1.array - embedding2.array) < DIFFERENCE_THRESHOLD


@pyutils.run_once
def get_cached_file_contents():
    with CACHED_MODEL_FILEPATH.open('rb') as f:
        return f.read()


def return_value_for_mock(mocker, val):
    mock = mocker.Mock()
    mock.get_file.return_value = val
    return mock


# TODO EFRS-103: fix the test according to new service logic
@pytest.mark.integration
def test_integration__when_calculating_embeddings_of_two_images_with_the_same_face__then_returns_similar_embeddings(
        mocker):
    mocker.patch('src.scan_faces._calc_embedding.calculator.get_storage',
                 return_value=return_value_for_mock(mocker, get_cached_file_contents()))
    person_a_im1 = imageio.imread(CURRENT_DIR / 'files' / 'personA-img1-cropped.jpg')
    person_a_im2 = imageio.imread(CURRENT_DIR / 'files' / 'personA-img2-cropped.jpg')

    person_a_face_embedding1 = calculate_embedding(person_a_im1)
    person_a_face_embedding2 = calculate_embedding(person_a_im2)

    assert embeddings_are_the_same(person_a_face_embedding1, person_a_face_embedding2)


# TODO: fix the test according to new service logic
@pytest.mark.integration
def test_integration__when_calculating_embeddings_of_two_images_with_different_faces__then_returns_different_embeddings(
        mocker):
    mocker.patch('src.scan_faces._calc_embedding.calculator.get_storage',
                 return_value=return_value_for_mock(mocker, get_cached_file_contents()))
    person_a_im = imageio.imread(CURRENT_DIR / 'files' / 'personA-img1-cropped.jpg')
    person_b_im = imageio.imread(CURRENT_DIR / 'files' / 'personB-img1-cropped.jpg')

    person_a_face_embedding = calculate_embedding(person_a_im)
    person_b_face_embedding = calculate_embedding(person_b_im)

    assert not embeddings_are_the_same(person_a_face_embedding, person_b_face_embedding)
