import attr
import numpy as np
from numpy.core.multiarray import ndarray

from src.face_recognition.dto.face_embedding import Embedding


@attr.s(auto_attribs=True)
class _FaceBase:
    name: str


@attr.s(auto_attribs=True)
class FaceEmbedding(_FaceBase):
    embedding: Embedding


@attr.s(auto_attribs=True, cmp=False)
class Face(FaceEmbedding):
    """
    >>> arr1, arr2, arr3 = np.zeros(shape=(1,)), np.ones(shape=(1,)), np.zeros(shape=(2,))
    >>> face1 = Face('name', arr1, '', arr1, arr1)
    >>> face2 = Face('name', arr2, '', arr1, arr1)
    >>> face3 = Face('name', arr3, '', arr1, arr1)
    >>> face4 = Face('other', arr1, '', arr1, arr1)
    >>> face1 == face1, face1 != face1, face1 == face2, face1 == face3, face1 == face4
    (True, False, False, False, False)
    """
    raw_img: ndarray
    face_img: ndarray

    def __eq__(self, other):
        if not isinstance(other, Face):
            raise NotImplementedError

        return (self.name == other.name
                and np.array_equal(self.face_img, other.face_img)
                and np.array_equal(self.raw_img, other.raw_img))
