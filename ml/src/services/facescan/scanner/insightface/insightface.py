import logging
from typing import List

import insightface
import numpy as np

from src.exceptions import NoFaceFoundError
from src.services.dto.bounding_box import BoundingBox
from src.services.dto.scanned_face import ScannedFace
from src.services.facescan.imgscaler.imgscaler import ImgScaler
from src.services.facescan.scanner.constants import NO_LIMIT
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.imgtools.types import Array3D


class InsightFace(FaceScanner):
    ID = 'InsightFace'
    IMG_LENGTH_LIMIT = 1000

    def __init__(self):
        super().__init__()
        self._model = insightface.app.FaceAnalysis()
        self._CTX_ID_CPU = -1
        self._NMS = 0.4

    def scan(self, img: Array3D, face_limit: int = NO_LIMIT, detection_threshold: float = None) -> List[ScannedFace]:
        self._model.prepare(ctx_id=self._CTX_ID_CPU, nms=self._NMS)
        scaler = ImgScaler(self.IMG_LENGTH_LIMIT)
        downscaled_img = scaler.downscale_img(img)
        results = self._model.get(downscaled_img, det_thresh=detection_threshold)
        scanned_faces = []
        for result in results:
            downscaled_box_array = result.bbox.astype(np.int).flatten()
            box = scaler.upscale_box(BoundingBox(x_min=downscaled_box_array[0],
                                                 y_min=downscaled_box_array[1],
                                                 x_max=downscaled_box_array[2],
                                                 y_max=downscaled_box_array[3],
                                                 probability=result.det_score))
            logging.debug("[Found face] "
                          f"Age: {result.age}, "
                          f"Gender: {'Male' if result.gender else 'Female'}, "
                          f"BBox: {box}")
            scanned_faces.append(ScannedFace(box=box, embedding=result.embedding, img=img))

        if len(scanned_faces) == 0:
            raise NoFaceFoundError
        if face_limit:
            return scanned_faces[:face_limit]
        return scanned_faces
