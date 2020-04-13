import joblib
import numpy
import numpy as np
import pytest

from src.exceptions import OneDimensionalImageIsGivenError, ImageReadLibraryError
from src.services.imgtools.read_img import read_img
from src.services.imgtools.test.files import IMG_DIR
from src.services.utils.pytestutils import raises


@pytest.fixture(scope='module')
def expected_array():
    return joblib.load(IMG_DIR / 'einstein.joblib')


def test__given_1d_img__when_read__then_raises_error(mocker):
    array = np.ones(shape=(10,))
    imageio = mocker.patch('src.services.imgtools.read_img.imageio')
    imageio.imread.return_value = array

    def act():
        read_img('mock.jpg')

    assert raises(OneDimensionalImageIsGivenError, act)


def test__given_grayscale_2d_img__when_read__then_returns_rgb_img(mocker):
    array = np.ones(shape=(10, 10))
    imageio = mocker.patch('src.services.imgtools.read_img.imageio')
    imageio.imread.return_value = array

    actual_img = read_img('mock.jpg')

    expected_img = np.ones(shape=(10, 10, 3))
    assert (actual_img == expected_img).all()


def test__given_rgba_img__when_read__then_returns_rgb_img(mocker):
    array = np.ones(shape=(10, 10, 4))
    imageio = mocker.patch('src.services.imgtools.read_img.imageio')
    imageio.imread.return_value = array

    actual_img = read_img('mock.jpg')

    expected_img = np.ones(shape=(10, 10, 3))
    assert (actual_img == expected_img).all()


@pytest.mark.parametrize('file', ['empty.png', 'corrupted.png'])
def test__given_corrupted_img__when_read__then_raises_exception(file):
    pass

    def act():
        read_img(IMG_DIR / file)

    assert raises(ImageReadLibraryError, act)


@pytest.mark.parametrize('extension', ['jpeg', 'png', 'tiff', 'ico', 'gif', 'bmp', 'webp'])
def test__given_img__when_read__then_returns_correct_numpy_array(extension, expected_array):
    img_path = IMG_DIR / f'einstein.{extension}'

    actual_array = read_img(img_path)

    assert actual_array.shape == expected_array.shape
    assert numpy.allclose(actual_array, expected_array, atol=20, rtol=20)
