from src.app import JSONEncodable
from src.dto.bounding_box import BoundingBox
from src.dto.scanned_face_dto import ScannedFaceDTO
from src.services.utils.nputils import Array1D, Array3D, crop_img


class ScannedFace(JSONEncodable):
    def __init__(self, box: BoundingBox, embedding: Array1D, img: Array3D, face_img: Array3D = None):
        self.box = box
        self.embedding = embedding
        self.img = img
        self._face_img = face_img

    @property
    def face_img(self):
        if not self._face_img:
            self._face_img = crop_img(self.img, self.box)
        return self._face_img

    def dto(self):
        return ScannedFaceDTO(self.box, self.embedding)
