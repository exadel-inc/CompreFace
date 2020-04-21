import logging
from typing import List

import numpy as np
from insightface.app import FaceAnalysis
from insightface.model_zoo import model_zoo
from insightface.utils import face_align

from src.constants import ENV
from src.services.dto.bounding_box import BoundingBox
from src.services.dto.scanned_face import ScannedFace
from src.services.facescan.imgscaler.imgscaler import ImgScaler
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.imgtools.types import Array3D

logger = logging.getLogger(__name__)


class InsightFace(FaceScanner):
    ID = 'InsightFace'
    DETECTION_MODEL_NAME = 'retinaface_r50_v1'
    CALCULATION_MODEL_NAME = 'arcface_r100_v1'
    IMG_LENGTH_LIMIT = ENV.IMG_LENGTH_LIMIT

    def __init__(self):
        super().__init__()
        self._detection_model = FaceAnalysis(det_name=self.DETECTION_MODEL_NAME, rec_name=None, ga_name=None)
        self._calculation_model = model_zoo.get_model(self.CALCULATION_MODEL_NAME)
        self._CTX_ID_CPU = -1
        self._NMS = 0.4
        self._detection_model.prepare(ctx_id=self._CTX_ID_CPU, nms=self._NMS)
        self._calculation_model.prepare(ctx_id=self._CTX_ID_CPU)
        self.det_prob_threshold = 0.8

    def scan(self, img: Array3D, det_prob_threshold: float = None) -> List[ScannedFace]:
        if det_prob_threshold is None:
            det_prob_threshold = self.det_prob_threshold
        assert 0 <= det_prob_threshold <= 1
        scaler = ImgScaler(self.IMG_LENGTH_LIMIT)
        downscaled_img = scaler.downscale_img(img)
        results = self._detection_model.get(downscaled_img, det_thresh=det_prob_threshold)
        scanned_faces = []
        for result in results:
            downscaled_box_array = result.bbox.astype(np.int).flatten()
            box = scaler.upscale_box(BoundingBox(x_min=downscaled_box_array[0],
                                                 y_min=downscaled_box_array[1],
                                                 x_max=downscaled_box_array[2],
                                                 y_max=downscaled_box_array[3],
                                                 probability=result.det_score))
            if box.probability <= det_prob_threshold:
                logger.debug(f'Box Filtered out because below threshold ({det_prob_threshold}: {box})')
                continue
            logger.debug(f"Found: {box}")

            norm_cropped_img = face_align.norm_crop(img, landmark=scaler.upscale_array(result.landmark))
            embedding = self._calculation_model.get_embedding(norm_cropped_img).flatten()

            scanned_faces.append(ScannedFace(box=box, embedding=embedding, img=img))
        return scanned_faces
