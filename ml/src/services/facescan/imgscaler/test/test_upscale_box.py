import numpy as np

from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.facescan.imgscaler.imgscaler import ImgScaler


def test__given_downscaled_image__when_upscaling_box__then_returns_upscaled_box():
    img = np.zeros((200, 200, 3))
    scaler = ImgScaler(img_length_limit=100)
    scaler.downscale_img(img)

    output_box = BoundingBoxDTO(10, 10, 20, 20, 1).scaled(scaler.upscale_coefficient)

    assert output_box == BoundingBoxDTO(20, 20, 40, 40, 1)


def test__given_not_downscaled_image__when_upscaling_box__then_returns_same_box():
    img = np.zeros((20, 20, 3))
    scaler = ImgScaler(img_length_limit=100)
    scaler.downscale_img(img)

    output_box = BoundingBoxDTO(10, 10, 20, 20, 1).scaled(scaler.upscale_coefficient)

    assert output_box == BoundingBoxDTO(10, 10, 20, 20, 1)
