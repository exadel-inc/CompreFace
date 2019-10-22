import os
from pathlib import Path

import imageio

from src.face_recognition.face_cropper.cropper import crop_face

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))


def crop_img_file(filename):
    img = imageio.imread(CURRENT_DIR / f'{filename}.jpg')
    img = crop_face(img).img
    imageio.imwrite(CURRENT_DIR / f'{filename}-cropped.jpg', img)


if __name__ == '__main__':
    crop_img_file('personA-img1')
    crop_img_file('personA-img2')
    crop_img_file('personB-img1')
