import itertools
import os
from pathlib import Path

import imageio
import pytest
from PIL import Image

from face_recognition.face_cropper.exceptions import NoFaceFoundError
from face_recognition.face_cropper.test._img_utils import images_are_almost_the_same, ndarray_to_img
from src.face_recognition.face_cropper.cropper import crop_faces

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))


@pytest.fixture(scope='module')
def multiple_cropped_faces():
    im = imageio.imread(CURRENT_DIR / 'files' / 'multiple-faces.jpg')

    cropped_faces = crop_faces(im)

    return cropped_faces


@pytest.mark.integration
def test_integration__when_called_with_no_faces__then_raises_error():
    im = imageio.imread(CURRENT_DIR / 'files' / 'no-faces.jpg')

    def act():
        crop_faces(im)

    assert pytest.raises(NoFaceFoundError, act)


@pytest.mark.integration
def test__when_called_with_one_face__then_returns_one_cropped_face():
    im = imageio.imread(CURRENT_DIR / 'files' / 'one-face.jpg')

    cropped_faces = crop_faces(im)

    assert len(cropped_faces) == 1
    given_crop_img = ndarray_to_img(cropped_faces[0].img)
    expected_crop_img = Image.open(CURRENT_DIR / 'files' / 'one-face.cropped.jpg')
    assert images_are_almost_the_same(given_crop_img, expected_crop_img)


@pytest.mark.integration
def test_integration__given_limit_2__when_called_with_multiple_faces__then_returns_2_cropped_faces():
    im = imageio.imread(CURRENT_DIR / 'files' / 'multiple-faces.jpg')

    cropped_faces = crop_faces(im, face_lim=2)

    assert len(cropped_faces) == 2


@pytest.mark.integration
def test_integration__when_called_with_multiple_faces__then_returns_multiple_cropped_faces(multiple_cropped_faces):
    assert len(multiple_cropped_faces) > 1


@pytest.mark.xfail(reason="EGP-700")
@pytest.mark.integration
def test_integration__when_called_with_multiple_faces__then_all_returned_faces_must_be_different(
        multiple_cropped_faces):
    img_combinations = itertools.combinations((ndarray_to_img(f.img) for f in multiple_cropped_faces), 2)
    images_are_same = (images_are_almost_the_same(*pair) for pair in img_combinations)
    assert not any(images_are_same)


@pytest.mark.integration
def test_integration__when_called_with_multiple_faces__then_returns_all_faces(multiple_cropped_faces):
    assert len(multiple_cropped_faces) == 5
