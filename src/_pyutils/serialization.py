from io import BytesIO

import joblib
import matplotlib.pyplot as plt
from numpy.core.multiarray import ndarray


def serialize(obj: object) -> bytes:
    bytes_container = BytesIO()
    joblib.dump(obj, bytes_container)  # Works better with numpy arrays than pickle
    bytes_container.seek(0)  # update to enable reading
    bytes_data = bytes_container.read()
    return bytes_data


def deserialize(bytes_data: bytes) -> object:
    bytes_container = BytesIO(bytes_data)
    obj = joblib.load(bytes_container)
    return obj


def numpy_to_jpg_file(img: ndarray):
    bytes_container = BytesIO()
    plt.imsave(bytes_container, img, format='jpg')
    return bytes_container.getvalue()
