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

import logging
from typing import List, Tuple

import attr
import cv2
import numpy as np
import mxnet as mx
from cached_property import cached_property
from insightface.app import FaceAnalysis
from insightface.model_zoo import (model_store, face_detection,
                                   face_recognition, face_genderage)
from insightface.utils import face_align

from src.constants import ENV
from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.dto.json_encodable import JSONEncodable
from src.services.facescan.imgscaler.imgscaler import ImgScaler
from src.services.facescan.plugins import base, mixins, exceptions
from src.services.facescan.plugins.insightface import helpers as insight_helpers
from src.services.dto import plugin_result
from src.services.imgtools.types import Array3D


logger = logging.getLogger(__name__)


class InsightFaceMixin:
    _CTX_ID = ENV.GPU_IDX
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


class FaceDetector(InsightFaceMixin, mixins.FaceDetectorMixin, base.BasePlugin):
    ml_models = (
        ('retinaface_r50_v1', '1h5rHDGE7qXC3jZwphObh9mW55YQYKY8Y'),
        ('retinaface_mnet025_v1', '1ggNFFqpe0abWz6V1A82rnxD6fyxB8W2c'),
        ('retinaface_mnet025_v2', '1EYTMxgcNdlvoL1fSC8N1zkaWrX75ZoNL'),
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

    def find_faces(self, img: Array3D, det_prob_threshold: float = None) -> List[BoundingBoxDTO]:
        if det_prob_threshold is None:
            det_prob_threshold = self.det_prob_threshold
        assert 0 <= det_prob_threshold <= 1
        scaler = ImgScaler(self.IMG_LENGTH_LIMIT)
        img = scaler.downscale_img(img)
        results = self._detection_model.get(img, det_thresh=det_prob_threshold)
        boxes = []
        for result in results:
            downscaled_box_array = result.bbox.astype(np.int).flatten()
            downscaled_box = BoundingBoxDTO(x_min=downscaled_box_array[0],
                                            y_min=downscaled_box_array[1],
                                            x_max=downscaled_box_array[2],
                                            y_max=downscaled_box_array[3],
                                            probability=result.det_score,
                                            np_landmarks=result.landmark)
            box = downscaled_box.scaled(scaler.upscale_coefficient)
            if box.probability <= det_prob_threshold:
                logger.debug(f'Box Filtered out because below threshold ({det_prob_threshold}: {box})')
                continue
            logger.debug(f"Found: {box}")
            boxes.append(box)
        return boxes

    def crop_face(self, img: Array3D, box: BoundingBoxDTO) -> Array3D:
        return face_align.norm_crop(img, landmark=box._np_landmarks,
                                    image_size=self.IMAGE_SIZE)


class Calculator(InsightFaceMixin, mixins.CalculatorMixin, base.BasePlugin):
    ml_models = (
        ('arcface_r100_v1', '11xFaEHIQLNze3-2RUV1cQfT-q6PKKfYp'),
        ('arcface_resnet34', '1ECp5XrLgfEAnwyTYFEhJgIsOAw6KaHa7'),
        ('arcface_resnet50', '1a9nib4I9OIVORwsqLB0gz0WuLC32E8gf'),
        ('arcface_mobilefacenet', '17TpxpyHuUc1ZTm3RIbfvhnBcZqhyKszV'),
        ('arcface-r50-msfdrop75', '1gNuvRNHCNgvFtz7SjhW82v2-znlAYaRO'),
        ('arcface-r100-msfdrop75', '1lAnFcBXoMKqE-SkZKTmi6MsYAmzG0tFw'),
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
class GenderAgeDTO(JSONEncodable):
    gender: str
    age: Tuple[int, int]


class GenderAgeDetector(InsightFaceMixin, base.BasePlugin):
    slug = 'gender_age'
    ml_models = (
        ('genderage_v1', '1J9hqSWqZz6YvMMNrDrmrzEW9anhvdKuC'),
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


class LandmarksDetector(mixins.LandmarksDetectorMixin, base.BasePlugin):
    """ Extract landmarks from FaceDetector results."""


class Landmarks2d106DTO(plugin_result.LandmarksDTO):
    """ 
    106-points facial landmarks 

    Points mark-up - https://github.com/deepinsight/insightface/tree/master/alignment/coordinateReg#visualization
    """
    NOSE_POSITION = 86


class Landmarks2d106Detector(InsightFaceMixin, mixins.LandmarksDetectorMixin,
                              base.BasePlugin):
    slug = 'landmark2d106'
    ml_models = (
        ('2d106det', '1MBWbTEYRhZFzj_O2f2Dc6fWGXFWtbMFw'),
    )
    CROP_SIZE = (192, 192) # model requirements

    def __call__(self, face: plugin_result.FaceDTO):
        landmarks = insight_helpers.predict_landmark2d106(
            self._landmark_model, face._img, self.CROP_SIZE,
            face.box.center, (face.box.width, face.box.height),
        )
        return Landmarks2d106DTO(landmarks=landmarks.astype(int).tolist())

    @cached_property
    def _landmark_model(self):
        model_prefix = f'{self.ml_model.path}/{self.ml_model.name}'
        sym, arg_params, aux_params = mx.model.load_checkpoint(model_prefix, 0)
        ctx = mx.gpu(self._CTX_ID) if self._CTX_ID >= 0 else mx.cpu()
        all_layers = sym.get_internals()
        sym = all_layers['fc1_output']
        model = mx.mod.Module(symbol=sym, context=ctx, label_names=None)
        model.bind(for_training=False,
                   data_shapes=[('data', (1, 3, *self.CROP_SIZE))])
        model.set_params(arg_params, aux_params)
        return model
