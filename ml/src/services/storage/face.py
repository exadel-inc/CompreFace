from typing import Union

import attr
import numpy as np

from src.services.imgtools.types import Array1D, Array3D

Array1D = Union[Array1D, np.ndarray]
Array3D = Union[Array3D, np.ndarray]


@attr.s(auto_attribs=True, frozen=True, cmp=False)
class FaceNameEmbedding:
    name: str
    embedding: Array1D


@attr.s(auto_attribs=True, frozen=True, cmp=False)
class Face(FaceNameEmbedding):
    raw_img: Array3D
    face_img: Array3D
