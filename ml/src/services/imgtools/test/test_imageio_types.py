import joblib
import numpy
import pytest

from src.services.imgtools.read_img import read_img
from src.services.utils.pyutils import get_dir

CURRENT_DIR = get_dir(__file__)


@pytest.mark.parametrize('img',
                         ["001_A.jpg", "001_A.jpeg", "001_A.png", "001_A.tiff", "001_A.ico", "001_A.gif", "001_A.bmp",
                          "001_A.tif", "001_A.webp"])
def test_different_formats_processed_are_right(img):
    file = CURRENT_DIR / "test_files" / img
    actual_array = read_img(file)
    load_array = CURRENT_DIR / "test_files" / "dump_array.pkl"
    expected_array = joblib.load(load_array)

    assert (actual_array.shape == expected_array.shape)
    assert numpy.allclose(actual_array, expected_array, atol=20, rtol=20)
