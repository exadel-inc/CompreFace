import attr

from src.services.utils.nputils import Array1D, Array3D


@attr.s(auto_attribs=True, frozen=True, cmp=False)
class FaceNameEmbedding:
    name: str
    embedding: Array1D


@attr.s(auto_attribs=True, frozen=True, cmp=False)
class Face(FaceNameEmbedding):
    raw_img: Array3D
    face_img: Array3D
