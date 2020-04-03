import numpy as np

from src.services.facescan.imgscaler.imgscaler import ImgScaler


def test__given_big_vertical_image__when_downscaling_img__then_returns_downscaled_by_height_img():
    img = np.zeros((300, 100, 3))
    scaler = ImgScaler(img_length_limit=100)

    scaled_img = scaler.downscale_img(img)

    assert scaled_img.shape == (100, 33, 3)


def test__given_big_horizontal_image__when_downscaling_img__then_returns_downscaled_by_width_img():
    img = np.zeros((100, 300, 3))
    scaler = ImgScaler(img_length_limit=100)

    scaled_img = scaler.downscale_img(img)

    assert scaled_img.shape == (33, 100, 3)


def test__given_small_image__when_downscaling_img__then_returns_same_img():
    img = np.zeros((10, 20, 3))
    scaler = ImgScaler(img_length_limit=100)

    scaled_img = scaler.downscale_img(img)

    assert (scaled_img == img).all()
