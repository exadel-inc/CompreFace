import numpy as np

from src.services.dto.bounding_box import BoundingBox
from src.services.facescan.imgscaler.imgscaler import ImgScaler


def test__given_downscaled_vertical_image__when_upscaling_box__then_upscales_correctly():
    img = np.zeros((3000, 1000, 3))
    scaler = ImgScaler(img_length_limit=1000)
    scaler.downscale_img(img)

    output_box = scaler.upscale_box(BoundingBox(30, 100, 60, 200, 0.95))

    assert output_box == BoundingBox(10, 33, 20, 67, 0.95)


def test__given_downscaled_horizontal_image__when_upscaling_box__then_upscales_correctly():
    img = np.zeros((1000, 3000, 3))
    scaler = ImgScaler(img_length_limit=1000)
    scaler.downscale_img(img)

    output_box = scaler.upscale_box(BoundingBox(30, 100, 60, 200, 0.95))

    assert output_box == BoundingBox(10, 33, 20, 67, 0.95)


def test__given_not_downscaled_image__when_upscaling_box__does_nothing():
    img = np.zeros((3000, 1000, 3))
    scaler = ImgScaler(img_length_limit=9999)
    scaler.downscale_img(img)

    output_box = scaler.upscale_box(BoundingBox(30, 100, 60, 200, 0.95))

    assert output_box == BoundingBox(30, 100, 60, 200, 0.95)
