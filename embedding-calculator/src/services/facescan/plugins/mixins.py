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

from time import time, sleep
from abc import ABC, abstractmethod
from contextlib import contextmanager
from typing import List, Tuple

from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.dto import plugin_result
from src.services.imgtools.types import Array3D
from src.services.facescan.plugins import base, exceptions


@contextmanager
def elapsed_time_contextmanager() -> int:
    """ Returns elapsed time in ms. """
    start = time()
    elapsed = 0
    yield lambda: elapsed
    # update variable after exit from context
    elapsed = int((time() - start) * 1000)


class FaceDetectorMixin(ABC):
    slug = 'detector'
    IMAGE_SIZE: int
    face_plugins: List[base.BasePlugin] = []

    def __call__(self, img: Array3D, det_prob_threshold: float = None,
                 face_plugins: Tuple[base.BasePlugin] = ()) -> List[plugin_result.FaceDTO]:
        """ Returns cropped and normalized faces."""
        faces = self._fetch_faces(img, det_prob_threshold)
        for face in faces:
            self._apply_face_plugins(face, face_plugins)
        return faces

    def _fetch_faces(self, img: Array3D, det_prob_threshold: float = None):
        with elapsed_time_contextmanager() as get_elapsed_time:
            boxes = self.find_faces(img, det_prob_threshold)
        return [
            plugin_result.FaceDTO(
                img=img, face_img=self.crop_face(img, box), box=box,
                execution_time={self.slug: get_elapsed_time() // len(boxes)}
            ) for box in boxes
        ]

    def _apply_face_plugins(self, face: plugin_result.FaceDTO,
                            face_plugins: Tuple[base.BasePlugin]):
        for plugin in face_plugins:
            try:
                with elapsed_time_contextmanager() as get_elapsed_time:
                    result_dto = plugin(face)
                face._plugins_dto.append(result_dto)
            except Exception as e:
                raise exceptions.PluginError(f'{plugin} error - {e}')
            else:
                face.execution_time[plugin.slug] = get_elapsed_time()

    @abstractmethod
    def find_faces(self, img: Array3D, det_prob_threshold: float = None) -> List[BoundingBoxDTO]:
        """ Find face bounding boxes, without calculating embeddings"""
        raise NotImplementedError

    @abstractmethod
    def crop_face(self, img: Array3D, box: BoundingBoxDTO) -> Array3D:
        """ Crop face by bounding box and resize/squish it """
        raise NotImplementedError


class CalculatorMixin(ABC):
    slug = 'calculator'
    # args for init MLModel: model name, Goodle Drive fileID, similarity coefficients
    ml_models: Tuple[Tuple[str, str, str], ...] = ()

    DIFFERENCE_THRESHOLD: float

    def __call__(self, face: plugin_result.FaceDTO) -> plugin_result.EmbeddingDTO:
        return plugin_result.EmbeddingDTO(
            embedding=self.calc_embedding(face._face_img)
        )

    def create_ml_model(self, *args):
        return base.CalculatorModel(self, *args)

    @abstractmethod
    def calc_embedding(self, face_img: Array3D) -> Array3D:
        """ Calculate embedding of a given face """
        raise NotImplementedError


class LandmarksDetectorMixin:
    slug = "landmarks"

    def __call__(self, face: plugin_result.FaceDTO) -> plugin_result.LandmarksDTO:
        return plugin_result.LandmarksDTO(landmarks=face.box.landmarks)
