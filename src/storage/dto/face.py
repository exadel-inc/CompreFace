import attr
import numpy as np
from numpy.core.multiarray import ndarray

from src.face_recognition.calc_embedding.calculator import calculate_embedding
from src.face_recognition.crop_faces.crop_faces import crop_one_face
from src.face_recognition.dto.embedding import Embedding


@attr.s(auto_attribs=True, frozen=True)
class _FaceBase:
    name: str


@attr.s(auto_attribs=True, frozen=True)
class FaceEmbedding(_FaceBase):
    """
    >>> FaceEmbedding('name', ...) == FaceEmbedding('name', ...)
    True
    >>> FaceEmbedding('name2', ...) == FaceEmbedding('name', ...)
    False
    >>> {FaceEmbedding('name', ...)} == {FaceEmbedding('name', ...)}
    True
    """
    embedding: Embedding


@attr.s(auto_attribs=True, frozen=True, cmp=False)
class Face(FaceEmbedding):
    """
    >>> arr, arr_2, arr_3 = np.zeros(shape=(1,)), np.ones(shape=(1,)), np.zeros(shape=(2,))
    >>> face = Face('name', ..., arr, arr)
    >>> face == face
    True
    >>> hash(face) == hash(face)
    True
    >>> face != face
    False
    >>> face == Face('other', ..., arr, arr)
    False
    >>> face == Face('other', None, arr, arr)
    False
    >>> face == Face('name', ..., arr, arr_2)
    False
    >>> face == Face('name', ..., arr, arr_3)
    False
    >>> {face, face} == {Face('name', ..., arr, arr), Face('name', ..., arr, arr)}
    True
    >>> {face, face} == {Face('name', ..., arr, arr), Face('other', ..., arr, arr)}
    False
    """
    raw_img: ndarray
    face_img: ndarray

    @classmethod
    def from_image(cls, face_name: str, img, detection_threshold_c):
        face_img = crop_one_face(img, detection_threshold_c).img
        embedding = calculate_embedding(face_img)
        return Face(face_name, embedding, img, face_img)

    def __hash__(self):
        return (hash(self.name)
                ^ hash(self.embedding)
                ^ hash(self.raw_img.data.tobytes())
                ^ hash(self.face_img.data.tobytes()))

    def __eq__(self, other):
        if not isinstance(other, Face):
            raise NotImplementedError

        return (self.name == other.name
                and self.embedding == other.embedding
                and np.array_equal(self.face_img, other.face_img)
                and np.array_equal(self.raw_img, other.raw_img))
