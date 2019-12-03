# TODO EFRS-103
#  - Make these tests use detect_faces function instead of crop_faces
import itertools
import os
from pathlib import Path

import imageio
import pytest
from PIL import Image
from numpy.core.multiarray import ndarray

from main import ROOT_DIR
from src.scan_faces.scan_faces import crop_faces
from src.scan_faces._detect_faces.exceptions import NoFaceFoundError, IncorrectImageDimensionsError
from src.scan_faces._detect_faces.test._img_utils import ndarray_to_img, images_are_almost_the_same
from src.pyutils.raises import raises

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))

IMG_DIR = ROOT_DIR / 'test_files'


@pytest.fixture(scope='module')
def cropped_faces_result_5faces():
    im = imageio.imread(IMG_DIR / 'five-faces.png')

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
    im = imageio.imread(IMG_DIR / 'no-faces.jpg')

    def act():
        crop_faces(im)

    assert raises(NoFaceFoundError, act)


@pytest.mark.integration
def test_integration__when_called_with_one_face__then_returns_one_cropped_face():
    im = imageio.imread(IMG_DIR / 'one-face.jpg')

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
    im = imageio.imread(IMG_DIR / 'five-faces.jpg')

    cropped_faces = crop_faces(im, face_limit=2)

    assert len(cropped_faces) == 2


@pytest.mark.integration
def test_integration__when_called_with_multiple_faces__then_all_returned_faces_must_be_different(
        cropped_faces_result_5faces):
    img_combinations = itertools.combinations((ndarray_to_img(face.img) for face in cropped_faces_result_5faces), r=2)
    images_are_same = (images_are_almost_the_same(*pair) for pair in img_combinations)
    assert not any(images_are_same)


@pytest.mark.integration
def test_integration__when_called_with_multiple_faces__then_returns_correct_amount_of_results(
        cropped_faces_result_5faces):
    assert len(cropped_faces_result_5faces) == 5


@pytest.mark.integration
def test_test_if_the_same_number_of_faces_png_vs_jpg():
    img_png = imageio.imread(IMG_DIR / 'eight-faces.png')
    img_jpg = imageio.imread(IMG_DIR / 'eight-faces.jpg')

    cropped_faces_png = crop_faces(img_png)
    cropped_faces_jpg = crop_faces(img_jpg)

    assert len(cropped_faces_png) == len(cropped_faces_jpg)
