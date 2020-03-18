from abc import ABC
from typing import List

from src.shared.facescan.types import Img, ScannedFace, ReturnLimit, ReturnLimitConstant


class BackendBase(ABC):
    ID = None

    def __init__(self):
        assert self.ID

    def scan(self,
             img: Img,
             return_limit: ReturnLimit = ReturnLimitConstant.NO_LIMIT,
             detection_threshold_c: float = None,
             return_cropped_img: bool = False) -> List[ScannedFace]:
        raise NotImplementedError
