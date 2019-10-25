import itertools
import os
from pathlib import Path

import imageio
import pytest
from PIL import Image
from numpy.core.multiarray import ndarray

from src.face_recognition.face_cropper.cropper import crop_faces
from src.face_recognition.face_cropper.exceptions import IncorrectImageDimensionsError, NoFaceFoundError
from src.face_recognition.face_cropper.test._img_utils import ndarray_to_img, images_are_almost_the_same
from src.pyutils.pytest_utils import raises

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))


@pytest.fixture(scope='module')
def cropped_faces_result_5faces():
    im = imageio.imread(CURRENT_DIR / 'files' / 'five-faces.jpg')

    cropped_faces = crop_faces(im)

    return cropped_faces


@pytest.mark.integration
def test_integration__when_called_with_less_than_2dimensional_image__then_raises_error():
    im = ndarray(shape=(10,))

    def act():
        crop_faces(im)

    assert raises(IncorrectImageDimensionsError, act)


@pytest.mark.integration
def test_integration__when_called_with_no_faces__then_raises_error():
    im = imageio.imread(CURRENT_DIR / 'files' / 'no-faces.jpg')

    def act():
        crop_faces(im)

    assert raises(NoFaceFoundError, act)


@pytest.mark.integration
def test_integration__when_called_with_one_face__then_returns_one_cropped_face():
    im = imageio.imread(CURRENT_DIR / 'files' / 'one-face.jpg')

    cropped_faces = crop_faces(im)

    assert len(cropped_faces) == 1
    given_crop_img = ndarray_to_img(cropped_faces[0].img)
    expected_crop_img = Image.open(CURRENT_DIR / 'files' / 'one-face.cropped.jpg')
    assert images_are_almost_the_same(given_crop_img, expected_crop_img)


@pytest.mark.integration
def test_integration__when_called_with_multiple_faces__then_returns_multiple_items(cropped_faces_result_5faces):
    assert len(cropped_faces_result_5faces) > 1


@pytest.mark.integration
def test_integration__given_limit_2__when_called_with_multiple_faces__then_returns_2_items():
    im = imageio.imread(CURRENT_DIR / 'files' / 'five-faces.jpg')

    cropped_faces = crop_faces(im, face_lim=2)

    assert len(cropped_faces) == 2


@pytest.mark.xfail(reason="TODO EGP-703")
@pytest.mark.integration
def test_integration__when_called_with_multiple_faces__then_returns_correct_amount_of_results(cropped_faces_result_5faces):
    assert len(cropped_faces_result_5faces) == 5


@pytest.mark.xfail(reason="TODO EGP-700")
@pytest.mark.integration
def test_integration__when_called_with_multiple_faces__then_all_returned_faces_must_be_different(
        cropped_faces_result_5faces):
    img_combinations = itertools.combinations((ndarray_to_img(f.img) for f in cropped_faces_result_5faces), r=2)
    images_are_same = (images_are_almost_the_same(*pair) for pair in img_combinations)
    assert not any(images_are_same)
