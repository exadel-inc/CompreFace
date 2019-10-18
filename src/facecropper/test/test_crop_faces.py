import os

import imageio

from src.facecropper import crop_faces

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))


def test__when_given_image_with_multiple_faces__then_returns_multiple_cropped_faces():
    im = imageio.imread(f'{SCRIPT_DIR}/files/multiple-faces.jpg')

    cropped_faces = crop_faces(im)

    assert len(cropped_faces) > 3


def test__given_limit_3__when_given_image_with_multiple_faces__then_returns_3_cropped_faces():
    im = imageio.imread(f'{SCRIPT_DIR}/files/multiple-faces.jpg')

    cropped_faces = crop_faces(im, face_lim=3)

    assert len(cropped_faces) == 3
