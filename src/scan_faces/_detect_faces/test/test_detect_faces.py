# TODO EFRS-103
#  - Make these tests use detect_faces function instead of crop_faces
import itertools
import os
from pathlib import Path

import imageio
import pytest
from numpy.core.multiarray import ndarray

from main import ROOT_DIR
from src.scan_faces.scan_faces import detect_faces, _preprocess_img
from src.scan_faces._detect_faces.exceptions import NoFaceFoundError, IncorrectImageDimensionsError
from src.scan_faces._detect_faces.test._img_utils import boxes_are_almost_the_same
from src.pyutils.raises import raises
from src.scan_faces._detect_faces.constants import FaceLimitConstant, DEFAULT_THRESHOLD_C
from src.scan_faces.dto.cropped_face import DetectedFace
from src.scan_faces.dto.bounding_box import BoundingBox

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))

IMG_DIR = ROOT_DIR / 'test_files'


@pytest.fixture(scope='module')
def detected_faces_result_5faces():
    im = imageio.imread(IMG_DIR / 'five-faces.jpg')

    detected_faces = detect_faces(im,  face_limit=FaceLimitConstant.NO_LIMIT, detection_threshold_c=DEFAULT_THRESHOLD_C)

    return detected_faces


@pytest.mark.integration
def test_integration__when_called_with_less_than_2dimensional_image__then_raises_error():
    im = ndarray(shape=(10,))

    def act():
        detect_faces(im, face_limit=FaceLimitConstant.NO_LIMIT, detection_threshold_c=DEFAULT_THRESHOLD_C)

    assert raises(IncorrectImageDimensionsError, act)


@pytest.mark.integration
def test_integration__when_called_with_no_faces__then_raises_error():
    im = imageio.imread(IMG_DIR / 'no-faces.jpg')

    def act():
        detect_faces(im)

    assert raises(NoFaceFoundError, act)


@pytest.mark.integration
def test_integration__when_called_with_one_face__then_returns_one_cropped_face():
    im = imageio.imread(IMG_DIR / 'one-face.jpg')

    detected_faces = detect_faces(im)

    assert len(detected_faces) == 1
    assert detected_faces == [DetectedFace(box=BoundingBox(x_min=85, y_min=108, x_max=261, y_max=330, probability=0.9999908208847046))]



@pytest.mark.integration
def test_integration__when_called_with_multiple_faces__then_returns_multiple_items(detected_faces_faces_result_5faces):
    assert len(detected_faces_faces_result_5faces) > 1


@pytest.mark.integration
def test_integration__given_limit_2__when_called_with_multiple_faces__then_returns_2_items():
    im = imageio.imread(IMG_DIR / 'five-faces.jpg')

    detected_faces = detect_faces(im,  face_limit=2)

    assert len(detected_faces) == 2


@pytest.mark.integration
def test_integration__when_called_with_multiple_faces__then_all_returned_faces_must_be_different(
        detected_faces_result_5faces):
    img_combinations = itertools.combinations(([face.box.x_max, face.box.x_min, face.box.y_max, face.box.y_min, face.box.probability] for face in detected_faces_result_5faces), r=2)
    boxes_are_same = (boxes_are_almost_the_same(*pair) for pair in img_combinations)
    assert not any(images_are_same)


@pytest.mark.integration
def test_integration__when_called_with_multiple_faces__then_returns_correct_amount_of_results(
        detected_faces_result_5faces):
    assert len(detected_faces_result_5faces) == 5


@pytest.mark.integration
def test_test_if_the_same_number_of_faces_png_vs_jpg():
    img_png = imageio.imread(IMG_DIR / 'eight-faces.png')
    img_jpg = imageio.imread(IMG_DIR / 'eight-faces.jpg')
    img_png = _preprocess_img(img_png)


    detected_faces_jpg = detect_faces(img_jpg,  face_limit=FaceLimitConstant.NO_LIMIT, detection_threshold_c=DEFAULT_THRESHOLD_C)
    detected_faces_png = detect_faces(img_png, face_limit=FaceLimitConstant.NO_LIMIT,
                                      detection_threshold_c=DEFAULT_THRESHOLD_C)

    assert len(detected_faces_png) == len(detected_faces_jpg)
