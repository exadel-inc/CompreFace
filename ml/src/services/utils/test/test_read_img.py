import numpy as np
import pytest

from src.exceptions import OneDimensionalImageIsGivenError, ImageReadLibraryError
from src.services.utils.nputils import read_img
from src.services.utils.pytestutils import raises
from src.services.utils.pyutils import get_dir

CURRENT_DIR = get_dir(__file__)


def test__given_1d_img__when_read__then_raises_error(mocker):
    array = np.ones(shape=(10,))
    imageio = mocker.patch('src.services.utils.nputils.imageio')
    imageio.imread.return_value = array

    def act():
        read_img('some-image.jpg')

    assert raises(OneDimensionalImageIsGivenError, act)


def test__given_grayscale_2d_img__when_read__then_returns_rgb_img(mocker):
    array = np.ones(shape=(10, 10))
    imageio = mocker.patch('src.services.utils.nputils.imageio')
    imageio.imread.return_value = array

    actual_img = read_img('some-image.jpg')

    expected_img = np.ones(shape=(10, 10, 3))
    assert (actual_img == expected_img).all()


def test__given_rgba_img__when_read__then_returns_rgb_img(mocker):
    array = np.ones(shape=(10, 10, 4))
    imageio = mocker.patch('src.services.utils.nputils.imageio')
    imageio.imread.return_value = array

    actual_img = read_img('some-image.jpg')

    expected_img = np.ones(shape=(10, 10, 3))
    assert (actual_img == expected_img).all()


@pytest.mark.parametrize('file', ['test_read_img.empty.png',
                                  'test_read_img.corrupted.png'])
def test__given_corrupted_img__when_read__then_raises_exception(file):
    pass

    def act():
        read_img(CURRENT_DIR / file)

    assert raises(ImageReadLibraryError, act)
