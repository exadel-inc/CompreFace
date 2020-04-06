from abc import ABC, abstractmethod
from typing import List

from src.exceptions import MoreThanOneFaceFoundError, NoFaceFoundError
from src.services.dto.scanned_face import ScannedFace
from src.services.imgtools.types import Array3D


class FaceScanner(ABC):
    ID = None

    def __init__(self):
        assert self.ID

    @abstractmethod
    def scan(self, img: Array3D, det_prob_threshold: float = None) -> List[ScannedFace]:
        raise NotImplementedError

    def scan_one(self, img: Array3D,
                 det_prob_threshold: float = None) -> ScannedFace:
        results = self.scan(img=img, det_prob_threshold=det_prob_threshold)
        if len(results) > 1:
            raise MoreThanOneFaceFoundError
        if len(results) == 0:
            raise NoFaceFoundError
        return results[0]
