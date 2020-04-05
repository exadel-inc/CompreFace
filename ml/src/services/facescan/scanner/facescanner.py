from abc import ABC, abstractmethod
from typing import List

from src.exceptions import MoreThanOneFaceFoundError
from src.services.dto.scanned_face import ScannedFace
from src.services.facescan.scanner.constants import NO_LIMIT
from src.services.imgtools.types import Array3D


class FaceScanner(ABC):
    ID = None

    def __init__(self):
        assert self.ID

    @abstractmethod
    def scan(self, img: Array3D,
             face_limit: int = NO_LIMIT,
             detection_threshold: float = None,
             allow_no_found_faces: bool = False) -> List[ScannedFace]:
        raise NotImplementedError

    def scan_one(self, img: Array3D,
                 detection_threshold: float = None) -> ScannedFace:
        results = self.scan(img=img, detection_threshold=detection_threshold)
        if len(results) > 1:
            raise MoreThanOneFaceFoundError
        return results[0]
