import logging
from typing import List

import insightface
import numpy as np

from src.exceptions import NoFaceFoundError
from src.services.dto.bounding_box import BoundingBox
from src.services.facescan.backend.facescan_backend import FacescanBackend
from src.services.facescan.constants import NO_LIMIT
from src.services.dto.scanned_face import ScannedFace
from src.services.utils.nputils import Array3D


class InsightFace(FacescanBackend):
    ID = 'InsightFace_v0'

    def __init__(self):
        super().__init__()
        self._model = insightface.app.FaceAnalysis()
        self._CTX_ID_CPU = -1
        self._NMS = 0.4

    def scan(self, img: Array3D,
             face_limit: int = NO_LIMIT,
             detection_threshold: float = None) -> List[ScannedFace]:
        if detection_threshold:
            raise NotImplementedError('Detection threshold support is not yet implemented')

        self._model.prepare(ctx_id=self._CTX_ID_CPU, nms=self._NMS)

        results = self._model.get(img)
        scanned_faces = []
        for result in results:
            bbox = result.bbox.astype(np.int).flatten()
            logging.debug("[Found face] "
                          f"Age: {result.age}, "
                          f"Gender: {'M' if result.gender else 'F'}, "
                          f"BBox: {bbox}")
            face = ScannedFace(box=BoundingBox(x_min=bbox[0], y_min=bbox[1], x_max=bbox[2], y_max=bbox[3],
                                               probability=result.det_score),
                               embedding=result.embedding, img=img)
            scanned_faces.append(face)

        if len(scanned_faces) == 0:
            raise NoFaceFoundError
        if face_limit:
            return scanned_faces[:face_limit]
        return scanned_faces
