import os

import imageio

from src.face_recognition import face_cropper

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))


def crop_img_file(filename):
    img = imageio.imread(f'{SCRIPT_DIR}/{filename}.jpg')
    img = face_cropper(img)
    imageio.imwrite(f'{SCRIPT_DIR}/{filename}-cropped.jpg', img)


if __name__ == '__main__':
    crop_img_file('personA-img1')
    crop_img_file('personA-img2')
    crop_img_file('personB-img1')
