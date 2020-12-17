#  Copyright (c) 2020 the original author or authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

import os
import logging
from typing import List, Tuple

import attr
import numpy as np
from cached_property import cached_property
from insightface.app import FaceAnalysis
from insightface.model_zoo import (model_store, face_detection,
                                   face_recognition, face_genderage)
from insightface.utils import face_align

from src.constants import ENV
from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.facescan.imgscaler.imgscaler import ImgScaler
from src.services.facescan.plugins import base, exceptions
from src.services.dto import plugin_result
from src.services.imgtools.types import Array3D


logger = logging.getLogger(__name__)


@attr.s(auto_attribs=True, frozen=True)
class InsightFaceBoundingBox(BoundingBoxDTO):
    landmark: Tuple[int, ...]

    @property
    def dto(self):
        return BoundingBoxDTO(x_min=self.x_min, x_max=self.x_max,
                              y_min=self.y_min, y_max=self.y_max,
                              probability=self.probability)

    def scaled(self, coefficient: float) -> 'InsightFaceBoundingBox':
        # noinspection PyTypeChecker
        return InsightFaceBoundingBox(x_min=self.x_min * coefficient, x_max=self.x_max * coefficient,
                                      y_min=self.y_min * coefficient, y_max=self.y_max * coefficient,
                                      probability=self.probability,
                                      landmark=self.landmark * coefficient)


class InsightFaceMixin:
    _CTX_ID = ENV.GPU_ID
    _NMS = 0.4

    def get_model_file(self, ml_model: base.MLModel):
        if not ml_model.exists():
            raise exceptions.ModelImportException(
                f'Model {ml_model.name} does not exists')
        return model_store.find_params_file(ml_model.path)


class DetectionOnlyFaceAnalysis(FaceAnalysis):
    rec_model = None
    ga_model = None

    def __init__(self, file):
        self.det_model = face_detection.FaceDetector(file, 'net3')


class FaceDetector(InsightFaceMixin, base.BaseFaceDetector):
    ml_models = (
        ('retinaface_r50_v1', 'http://insightface.ai/files/models/retinaface_r50_v1.zip'),
        ('retinaface_mnet025_v1', 'http://insightface.ai/files/models/retinaface_mnet025_v1.zip'),
        ('retinaface_mnet025_v2', 'http://insightface.ai/files/models/retinaface_mnet025_v2.zip'),
    )

    IMG_LENGTH_LIMIT = ENV.IMG_LENGTH_LIMIT
    IMAGE_SIZE = 112
    det_prob_threshold = 0.8

    @cached_property
    def _detection_model(self):
        model_file = self.get_model_file(self.ml_model)
        model = DetectionOnlyFaceAnalysis(model_file)
        model.prepare(ctx_id=self._CTX_ID, nms=self._NMS)
        return model

    def find_faces(self, img: Array3D, det_prob_threshold: float = None) -> List[InsightFaceBoundingBox]:
        if det_prob_threshold is None:
            det_prob_threshold = self.det_prob_threshold
        assert 0 <= det_prob_threshold <= 1
        scaler = ImgScaler(self.IMG_LENGTH_LIMIT)
        img = scaler.downscale_img(img)
        results = self._detection_model.get(img, det_thresh=det_prob_threshold)
        boxes = []
        for result in results:
            downscaled_box_array = result.bbox.astype(np.int).flatten()
            downscaled_box = InsightFaceBoundingBox(x_min=downscaled_box_array[0],
                                                    y_min=downscaled_box_array[1],
                                                    x_max=downscaled_box_array[2],
                                                    y_max=downscaled_box_array[3],
                                                    probability=result.det_score,
                                                    landmark=result.landmark)
            box = downscaled_box.scaled(scaler.upscale_coefficient)
            if box.probability <= det_prob_threshold:
                logger.debug(f'Box Filtered out because below threshold ({det_prob_threshold}: {box})')
                continue
            logger.debug(f"Found: {box.dto}")
            boxes.append(box)
        return boxes

    def crop_face(self, img: Array3D, box: InsightFaceBoundingBox) -> Array3D:
        return face_align.norm_crop(img, landmark=box.landmark,
                                    image_size=self.IMAGE_SIZE)


class Calculator(InsightFaceMixin, base.BaseCalculator):
    ml_models = (
        ('arcface_r100_v1', 'http://insightface.ai/files/models/arcface_r100_v1.zip'),
        ('arcface_mnet', 'https://drive.google.com/uc?id=1ejWgx_7Nd1PvFXPxCu_1_QNzKfl6v7Hb'),
    )

    DIFFERENCE_THRESHOLD = 400

    def calc_embedding(self, face_img: Array3D) -> Array3D:
        return self._calculation_model.get_embedding(face_img).flatten()

    @cached_property
    def _calculation_model(self):
        model_file = self.get_model_file(self.ml_model)
        model = face_recognition.FaceRecognition(
            self.ml_model.name, True,  model_file)
        model.prepare(ctx_id=self._CTX_ID)
        return model


@attr.s(auto_attribs=True, frozen=True)
class GenderAgeDTO(plugin_result.PluginResultDTO):
    gender: str
    age: Tuple[int, int]


class GenderAgeDetector(InsightFaceMixin, base.BasePlugin):
    slug = 'gender_age'
    ml_models = (
        ('genderage_v1', 'http://insightface.ai/files/models/genderage_v1.zip'),
    )

    GENDERS = ('female', 'male')

    def __call__(self, face_img: Array3D):
        gender, age = self._genderage_model.get(face_img)
        return GenderAgeDTO(gender=self.GENDERS[int(gender)], age=(age, age))

    @cached_property
    def _genderage_model(self):
        model_file = self.get_model_file(self.ml_model)
        model = face_genderage.FaceGenderage(
            self.ml_model.name, True, model_file)
        model.prepare(ctx_id=self._CTX_ID)
        return model
