import os
from pathlib import Path

import imageio
import pytest
from PIL import Image

from src.face_recognition.face_cropper.crop_face import crop_face
from src.face_recognition.face_cropper.exceptions import NoFaceFoundError
from src.face_recognition.face_cropper.test._img_utils import ndarray_to_img, images_are_the_same

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))


def test__when_given_image_with_no_faces__then_raises_error():
    im = imageio.imread(CURRENT_DIR / 'files' / 'no-faces.jpg')

    def act():
        crop_face(im)

    assert pytest.raises(NoFaceFoundError, act)


def test__when_given_image_with_one_faces__then_returns_one_cropped_face():
    im = imageio.imread(CURRENT_DIR / 'files' / 'one-face.jpg')

    crop_ndarray = crop_face(im)

    given_crop_img = ndarray_to_img(crop_ndarray)
    expected_crop_img = Image.open(CURRENT_DIR / 'files' / 'one-face.cropped.jpg')
    assert images_are_the_same(given_crop_img, expected_crop_img)
