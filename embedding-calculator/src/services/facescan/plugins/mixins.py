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

import cv2
import numpy as np
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
            # sort by face area
            boxes = sorted(boxes, key=lambda x: x.width * x.height, reverse=True)

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


class PoseEstimatorMixin:
    slug = 'pose'
    FOCAL_COEF = 1
    KEYPOINTS_3D = {
        'left_eye': [-8.0, 9.0, -8.0],
        'right_eye': [8.0, 9.0, -8.0],
        'nose': [0.0, 1.0, 0.0],
        'mouth_left': [-5.0, -4.0, -8.0],
        'mouth_right': [5.0, -4.0, -8.0],
        'chin': [0.0, -11.0, -4.0]
    }

    @staticmethod
    def add_chin_point(keypoints):
        keypoints['nose_bridge'] = (
            (keypoints['left_eye'][0] + keypoints['right_eye'][0]) // 2,
            (keypoints['left_eye'][1] + keypoints['right_eye'][1]) // 2
        )
        keypoints['mouth_center'] = (
            (keypoints['mouth_left'][0] + keypoints['mouth_right'][0]) // 2,
            (keypoints['mouth_left'][1] + keypoints['mouth_right'][1]) // 2
        )
        keypoints['chin'] = (
            keypoints['mouth_center'][0] + (keypoints['mouth_center'][0] - keypoints['nose_bridge'][0]) // 2,
            keypoints['mouth_center'][1] + (keypoints['mouth_center'][1] - keypoints['nose_bridge'][1]) // 2
        )
        return keypoints

    @staticmethod
    def camera_matrix(focal_length, optical_center):
        return np.array(
            [[focal_length, 1, optical_center[0]],
            [0, focal_length, optical_center[1]],
            [0, 0, 1]],
            dtype=np.float)

    @staticmethod
    def landmarks_names_ordered():
        """ List of lanmarks names orderred as in detector """
        raise NotImplementedError

    def __call__(self, face: plugin_result.FaceDTO) -> plugin_result.PoseDTO:
        keypoints_on_image = dict(zip(self.landmarks_names_ordered(), face.box.landmarks))
        keypoints_on_image = self.add_chin_point(keypoints_on_image)
        
        keypoints_on_image_array = np.array([keypoints_on_image[point_name] \
             for point_name in self.KEYPOINTS_3D.keys()], dtype=np.float)
        keypoints_3d_array = np.array(list(self.KEYPOINTS_3D.values()), dtype=np.float)

        image_height, image_width, channels_count = face._img.shape
        focal_length = self.FOCAL_COEF * image_width
        camera_matrix = self.camera_matrix(focal_length, (image_height / 2, image_width / 2))

        success, rotation_vector, translation_vector = cv2.solvePnP(
            keypoints_3d_array,
            keypoints_on_image_array,
            camera_matrix,
            np.zeros((4, 1), dtype=np.float64)
        )

        rotation_matrix, jacobian = cv2.Rodrigues(rotation_vector)
        angles, mtx_r, mtx_q, q_x, q_y, q_z = cv2.RQDecomp3x3(rotation_matrix)
        return plugin_result.PoseDTO(
            pitch=np.sign(angles[0]) * 180 - angles[0], 
            yaw=angles[1], 
            roll=np.sign(angles[2]) * 180 - angles[2])
