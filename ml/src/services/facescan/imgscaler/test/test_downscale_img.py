import numpy as np

from src.services.facescan.imgscaler.imgscaler import ImgScaler


def test__given_big_vertical_image__when_downscaling_img__downscales_by_vertical_dimension():
    img = np.zeros((3000, 1000, 3))
    scaler = ImgScaler(img_length_limit=1000)

    scaled_img = scaler.downscale_img(img)

    assert scaled_img.shape == (1000, 333, 3)


def test__given_big_horizontal_image__when_downscaling_img__downscales_by_horizontal_dimension():
    img = np.zeros((1000, 3000, 3))
    scaler = ImgScaler(img_length_limit=1000)

    scaled_img = scaler.downscale_img(img)

    assert scaled_img.shape == (333, 1000, 3)


def test__given_small_image__when_downscaling_img__does_nothing():
    img = np.zeros((100, 200, 3))
    scaler = ImgScaler(img_length_limit=1000)

    scaled_img = scaler.downscale_img(img)

    assert (scaled_img == img).all()
