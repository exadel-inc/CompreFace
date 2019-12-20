import os
from pathlib import Path

import imageio

from main import ROOT_DIR
from src.face_recognition.crop_faces.crop_faces import crop_one_face
from src.init_runtime import init_runtime

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
IMAGES_DIR = ROOT_DIR / 'test_files'


def crop_img_file(filename):
    img = imageio.imread(IMAGES_DIR / f'{filename}.jpg')
    img = crop_one_face(img).img
    imageio.imwrite(CURRENT_DIR / f'{filename}-cropped.jpg', img)


if __name__ == '__main__':
    init_runtime()
    crop_img_file('personA-img1')
    crop_img_file('personA-img2')
    crop_img_file('personB-img1')
