import logging
from typing import List

import insightface
import numpy as np

from src.constants import ENV
from src.exceptions import NoFaceFoundError
from src.services.dto.bounding_box import BoundingBox
from src.services.dto.scanned_face import ScannedFace
from src.services.facescan.imgscaler.imgscaler import ImgScaler
from src.services.facescan.scanner.constants import NO_LIMIT
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.imgtools.types import Array3D


class InsightFace(FaceScanner):
    ID = 'InsightFace'
    IMG_LENGTH_LIMIT = ENV.IMG_LENGTH_LIMIT

    def __init__(self):
        super().__init__()
        self._model = insightface.app.FaceAnalysis()
        self._CTX_ID_CPU = -1
        self._NMS = 0.4

    def scan(self, img: Array3D, face_limit: int = NO_LIMIT, detection_threshold: float = None,
             allow_no_found_faces: bool = False) -> List[ScannedFace]:
        self._model.prepare(ctx_id=self._CTX_ID_CPU, nms=self._NMS)
        scaler = ImgScaler(self.IMG_LENGTH_LIMIT)
        downscaled_img = scaler.downscale_img(img)
        if detection_threshold is not None:
            assert 0 <= detection_threshold <= 1
            results = self._model.get(downscaled_img, det_thresh=detection_threshold)
        else:
            results = self._model.get(downscaled_img)
        scanned_faces = []
        for result in results:
            downscaled_box_array = result.bbox.astype(np.int).flatten()
            box = scaler.upscale_box(BoundingBox(x_min=downscaled_box_array[0],
                                                 y_min=downscaled_box_array[1],
                                                 x_max=downscaled_box_array[2],
                                                 y_max=downscaled_box_array[3],
                                                 probability=result.det_score))
            logging.debug(f"Found: Age({result.age}) Gender({'Male' if result.gender else 'Female'}) {box}")
            scanned_faces.append(ScannedFace(box=box, embedding=result.embedding, img=img))
        if not allow_no_found_faces and len(scanned_faces) == 0:
            raise NoFaceFoundError
        if face_limit:
            return scanned_faces[:face_limit]
        return scanned_faces
