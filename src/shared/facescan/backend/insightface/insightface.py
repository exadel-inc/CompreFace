import logging
from typing import List

import insightface
import numpy as np

from src.shared.facescan.backend.backend_base import BackendBase
from src.shared.facescan.exceptions import NoFaceFoundError
from src.shared.facescan.types import Img, ScannedFace, BoundingBox, ReturnLimit, ReturnLimitConstant, ScannedFaceBase
from src.shared.utils.nputils import crop_image


class InsightFace(BackendBase):
    ID = 'InsightFace_v0'

    def __init__(self):
        super().__init__()
        self._model = insightface.app.FaceAnalysis()
        self._CTX_ID_CPU = -1
        self._NMS = 0.4

    def scan(self,
             img: Img,
             return_limit: ReturnLimit = ReturnLimitConstant.NO_LIMIT,
             detection_threshold_c: float = None,
             return_cropped_img: bool = False) -> List[ScannedFace]:
        assert detection_threshold_c is None

        self._model.prepare(ctx_id=self._CTX_ID_CPU, nms=self._NMS)

        results = self._model.get(img)
        scanned_faces = []
        for result in results:
            bbox = result.bbox.astype(np.int).flatten()
            logging.debug("[Found face] "
                          f"Age: {result.age}, "
                          f"Gender: {'M' if result.gender else 'F'}, "
                          f"BBox: {bbox}")
            face = ScannedFaceBase(
                box=BoundingBox(x_min=bbox[0],
                                y_min=bbox[1],
                                x_max=bbox[2],
                                y_max=bbox[3],
                                probability=result.det_score),
                embedding=result.embedding
            )
            if return_cropped_img:
                face = face.add_img(img=crop_image(img, bbox))
            scanned_faces.append(face)

        if len(scanned_faces) == 0:
            raise NoFaceFoundError("No face is found in the given image")
        if return_limit:
            return scanned_faces[:return_limit]
        return scanned_faces
