from abc import ABC, abstractmethod
from typing import List

import numpy as np

from src.exceptions import MoreThanOneFaceFoundError, NoFaceFoundError
from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.dto.scanned_face import ScannedFace
from src.services.imgtools.types import Array3D


class FaceScanner(ABC):
    ID = None

    def __init__(self):
        assert self.ID

    @abstractmethod
    def scan(self, img: Array3D, det_prob_threshold: float = None) -> List[ScannedFace]:
        raise NotImplementedError

    @abstractmethod
    def find_faces(self, img: Array3D, det_prob_threshold: float = None) -> List[BoundingBoxDTO]:
        raise NotImplementedError

    def scan_one(self, img: Array3D,
                 det_prob_threshold: float = None) -> ScannedFace:
        results = self.scan(img=img, det_prob_threshold=det_prob_threshold)
        if len(results) > 1:
            raise MoreThanOneFaceFoundError
        if len(results) == 0:
            raise NoFaceFoundError
        return results[0]


class MockScanner(FaceScanner):
    ID = 'MockScanner'

    def scan(self, img: Array3D, det_prob_threshold: float = None) -> List[ScannedFace]:
        return [ScannedFace(box=BoundingBoxDTO(0, 0, 0, 0, 0), embedding=np.random.rand(1), img=img, face_img=img)]

    def find_faces(self, img: Array3D, det_prob_threshold: float = None) -> List[BoundingBoxDTO]:
        return [BoundingBoxDTO(0, 0, 0, 0, 0)]
