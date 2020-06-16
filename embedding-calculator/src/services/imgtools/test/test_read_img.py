#  Copyright (c) 2020 the original author or authors
# 
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
# 
#       https://www.apache.org/licenses/LICENSE-2.0
# 
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

import joblib
import numpy
import numpy as np
import pytest

from src.exceptions import OneDimensionalImageIsGivenError, ImageReadLibraryError
from src.services.imgtools.read_img import read_img
from src.services.imgtools.test.files import IMG_DIR
from src.services.utils.pytestutils import raises

MOCK = 'mock.jpg'
IMAGEIO = 'src.services.imgtools.read_img.imageio'


@pytest.fixture(scope='module')
def expected_array():
    return joblib.load(IMG_DIR / 'einstein.joblib')


def test__given_1d_img__when_read__then_raises_error(mocker):
    array = np.ones(shape=(10,))
    imageio = mocker.patch(IMAGEIO)
    imageio.imread.return_value = array

    def act():
        read_img(MOCK)

    assert raises(OneDimensionalImageIsGivenError, act)


def test__given_grayscale_2d_img__when_read__then_returns_rgb_img(mocker):
    array = np.ones(shape=(10, 10))
    imageio = mocker.patch(IMAGEIO)
    imageio.imread.return_value = array

    actual_img = read_img(MOCK)

    expected_img = np.ones(shape=(10, 10, 3))
    assert (actual_img == expected_img).all()


def test__given_rgba_img__when_read__then_returns_rgb_img(mocker):
    array = np.ones(shape=(10, 10, 4))
    imageio = mocker.patch(IMAGEIO)
    imageio.imread.return_value = array

    actual_img = read_img(MOCK)

    expected_img = np.ones(shape=(10, 10, 3))
    assert (actual_img == expected_img).all()


@pytest.mark.parametrize('file', ['empty.png', 'corrupted.png'])
def test__given_corrupted_img__when_read__then_raises_exception(file):
    pass  # NOSONAR

    def act():
        read_img(IMG_DIR / file)

    assert raises(ImageReadLibraryError, act)


def test__given_truncated_img__when_read__then_does_not_crash():
    img_path = IMG_DIR / 'truncated.jpg'

    actual_array = read_img(img_path)

    assert actual_array.shape == (225, 225, 3)


@pytest.mark.parametrize('extension', ['jpeg', 'png', 'tiff', 'ico', 'gif', 'bmp', 'webp'])
def test__given_img__when_read__then_returns_correct_numpy_array(extension, expected_array):
    img_path = IMG_DIR / f'einstein.{extension}'

    actual_array = read_img(img_path)

    assert actual_array.shape == expected_array.shape
    assert numpy.allclose(actual_array, expected_array, atol=20, rtol=20)
