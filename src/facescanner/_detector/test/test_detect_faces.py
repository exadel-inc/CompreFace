import itertools
import os
from pathlib import Path

import imageio
import pytest
from numpy.core.multiarray import ndarray

from src._pyutils.raises import raises
from src.facescanner._detector.exceptions import NoFaceFoundError, IncorrectImageDimensionsError
from src.facescanner._detector.test._img_utils import boxes_are_almost_the_same
from src.facescanner.facescanner import scan_faces, _preprocess_img

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
IMG_DIR = CURRENT_DIR / '..' / '_files'


@pytest.fixture(scope='module')
def detected_faces_result_5faces():
    im = imageio.imread(IMG_DIR / 'five-faces.jpg')

    detected_faces = scan_faces(im)

    return detected_faces


@pytest.mark.integration
def test_integration__when_called_with_less_than_2dimensional_image__then_raises_error():
    im = ndarray(shape=(10,))

    def act():
        scan_faces(im)

    assert raises(IncorrectImageDimensionsError, act)


@pytest.mark.integration
def test_integration__when_called_with_no_faces__then_raises_error():
    im = imageio.imread(IMG_DIR / 'no-faces.jpg')

    def act():
        scan_faces(im)

    assert raises(NoFaceFoundError, act)


@pytest.mark.integration
def test_integration__when_called_with_one_face__then_detects_one_face():
    im = imageio.imread(IMG_DIR / 'one-face.jpg')
    check_box = [85, 108, 261, 330, 0.9999908208847046]

    detected_faces = scan_faces(im)

    assert len(detected_faces) == 1
    face = detected_faces[0].box
    assert boxes_are_almost_the_same([face.x_min, face.y_min, face.x_max, face.y_max, face.probability], check_box)


@pytest.mark.integration
def test_integration__given_limit_2__when_called_with_multiple_faces__then_detects_2_items():
    im = imageio.imread(IMG_DIR / 'five-faces.jpg')

    detected_faces = scan_faces(im, face_limit=2)

    assert len(detected_faces) == 2


@pytest.mark.integration
def test_integration__when_called_with_multiple_faces__then_all_detected_faces_must_be_different(
        detected_faces_result_5faces):
    box_combinations = itertools.combinations(
        ([face.box.x_max, face.box.x_min, face.box.y_max, face.box.y_min, face.box.probability] for face in
         detected_faces_result_5faces), r=2)
    boxes_are_same = (boxes_are_almost_the_same(*pair) for pair in box_combinations)
    assert not any(boxes_are_same)


@pytest.mark.integration
def test_integration__when_called_with_multiple_faces__then_detects_correct_amount_of_results(
        detected_faces_result_5faces):
    assert len(detected_faces_result_5faces) == 5


@pytest.mark.integration
def test_if_the_same_number_of_faces_png_vs_jpg():
    img_png = _preprocess_img(imageio.imread(IMG_DIR / 'eight-faces.png'))
    img_jpg = _preprocess_img(imageio.imread(IMG_DIR / 'eight-faces.jpg'))

    detected_faces_png = scan_faces(img_png)
    detected_faces_jpg = scan_faces(img_jpg)

    assert len(detected_faces_png) == len(detected_faces_jpg)
