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

from typing import Tuple, Union

import numpy as np
from cached_property import cached_property

from src.services.facescan.plugins.facenet.facenet import FaceDetector
from src.services.imgtools.types import Array3D
from src.services.facescan.plugins import base
from src.services.dto import plugin_result

import cv2


class PoseEstimator(base.BasePlugin):
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

    def __call__(self, face: plugin_result.FaceDTO):
        keypoints_on_image = dict(zip(FaceDetector.KEYPOINTS_ORDER, face.box.landmarks))
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
        return plugin_result.PoseDTO(angles)


