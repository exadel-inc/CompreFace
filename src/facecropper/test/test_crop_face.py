import os

import imageio
import pytest
from PIL import Image

from src.crop import crop_face
from src.crop.exceptions import NoFaceFoundError
from src.crop.test._img_utils import ndarray_to_img, images_are_the_same

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))


def test__when_given_image_with_no_faces__then_raises_error():
    im = imageio.imread(f'{SCRIPT_DIR}/files/no-faces.jpg')

    def act():
        crop_face(im)

    assert pytest.raises(NoFaceFoundError, act)


def test__when_given_image_with_one_faces__then_returns_one_cropped_face():
    im = imageio.imread(f'{SCRIPT_DIR}/files/one-face.jpg')

    crop_ndarray = crop_face(im)

    given_crop_img = ndarray_to_img(crop_ndarray)
    expected_crop_img = Image.open(f'{SCRIPT_DIR}/files/one-face.cropped.jpg')
    assert images_are_the_same(given_crop_img, expected_crop_img)
