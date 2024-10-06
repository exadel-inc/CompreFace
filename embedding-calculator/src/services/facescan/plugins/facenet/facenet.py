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
import math
from collections import namedtuple
from typing import List

import numpy as np
import tensorflow.compat.v1 as tf1
from tensorflow.python.platform import gfile
from cached_property import cached_property

import sys
sys.path.append('srcext')
from mtcnn import MTCNN

from src.constants import ENV
from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.facescan.plugins import mixins
from src.services.facescan.imgscaler.imgscaler import ImgScaler
from src.services.imgtools.proc_img import crop_img, squish_img
from src.services.imgtools.types import Array3D
from src.services.utils.pyutils import get_current_dir

from src.services.facescan.plugins import base
from src._endpoints import FaceDetection

CURRENT_DIR = get_current_dir(__file__)

logger = logging.getLogger(__name__)
_EmbeddingCalculator = namedtuple('_EmbeddingCalculator', 'graph sess')
_FaceDetectionNets = namedtuple('_FaceDetectionNets', 'pnet rnet onet')


def prewhiten(img):
    """ Normalize image."""
    mean = np.mean(img)
    std = np.std(img)
    std_adj = np.maximum(std, 1.0 / np.sqrt(img.size))
    y = np.multiply(np.subtract(img, mean), 1 / std_adj)
    return y


class FaceDetector(mixins.FaceDetectorMixin, base.BasePlugin):
    FACE_MIN_SIZE = 20
    SCALE_FACTOR = 0.709
    IMAGE_SIZE = 160
    IMG_LENGTH_LIMIT = ENV.IMG_LENGTH_LIMIT
    KEYPOINTS_ORDER = ['left_eye', 'right_eye', 'nose', 'mouth_left', 'mouth_right']

    # detection settings
    det_prob_threshold = 0.85
    det_threshold_a = 0.9436513301
    det_threshold_b = 0.7059968943
    det_threshold_c = 0.5506904359

    # face alignment settings (were calculated for current detector)
    left_margin = 0.2125984251968504
    right_margin = 0.2230769230769231
    top_margin = 0.10526315789473684
    bottom_margin = 0.09868421052631579

    @cached_property
    def _face_detection_net(self):
        return MTCNN(
            min_face_size=self.FACE_MIN_SIZE,
            scale_factor=self.SCALE_FACTOR,
            steps_threshold=[self.det_threshold_a, self.det_threshold_b, self.det_threshold_c]
        )

    def crop_face(self, img: Array3D, box: BoundingBoxDTO) -> Array3D:
        return squish_img(crop_img(img, box), (self.IMAGE_SIZE, self.IMAGE_SIZE))

    def find_faces(self, img: Array3D, det_prob_threshold: float = None) -> List[BoundingBoxDTO]:
        if det_prob_threshold is None:
            det_prob_threshold = self.det_prob_threshold
        assert 0 <= det_prob_threshold <= 1
        scaler = ImgScaler(self.IMG_LENGTH_LIMIT)
        img = scaler.downscale_img(img)

        if FaceDetection.SKIPPING_FACE_DETECTION:
            bounding_boxes = []
            bounding_boxes.append({
                'box': [0, 0, img.shape[0], img.shape[1]],
                'confidence': 1.0,
                'keypoints': {
                    'left_eye': (),
                    'right_eye': (),
                    'nose': (),
                    'mouth_left': (),
                    'mouth_right': (),
                }
            })
            det_prob_threshold = self.det_prob_threshold
            detect_face_result = bounding_boxes
        else:
            fdn = self._face_detection_net
            detect_face_result = fdn.detect_faces(img)

        img_size = np.asarray(img.shape)[0:2]
        bounding_boxes = []

        for face in detect_face_result:
            x, y, w, h = face['box']
            box = BoundingBoxDTO(
                x_min=int(np.maximum(x - (self.left_margin * w), 0)),
                y_min=int(np.maximum(y - (self.top_margin * h), 0)),
                x_max=int(np.minimum(x + w + (self.right_margin * w), img_size[1])),
                y_max=int(np.minimum(y + h + (self.bottom_margin * h), img_size[0])),
                np_landmarks=np.array([list(face['keypoints'][point_name]) for point_name in self.KEYPOINTS_ORDER]),
                probability=face['confidence']
            )
            logger.debug(f"Found: {box}")
            bounding_boxes.append(box)

        filtered_bounding_boxes = []
        for box in bounding_boxes:
            box = box.scaled(scaler.upscale_coefficient)
            if box.probability <= det_prob_threshold:
                logger.debug(f'Box filtered out because below threshold ({det_prob_threshold}): {box}')
                continue
            filtered_bounding_boxes.append(box)
        return filtered_bounding_boxes


class Calculator(mixins.CalculatorMixin, base.BasePlugin):
    ml_models = (
        # VGGFace2 training set, 0.9965 LFW accuracy
        ('20180402-114759', '1im5Qq006ZEV_tViKh3cgia_Q4jJ13bRK', (1.1817961, 5.291995557), 0.4),
        # CASIA-WebFace training set, 0.9905 LFW accuracy
        ('20180408-102900', '100w4JIUz44Tkwte9F-wEH0DOFsY-bPaw', (1.1362496, 5.803152427), 0.4),
        # CASIA-WebFace-Masked, 0.9873 LFW, 0.9667 LFW-Masked (orig model has 0.9350 on LFW-Masked)
        ('inception_resnetv1_casia_masked', '1FddVjS3JbtUOjgO0kWs43CAh0nJH2RrG', (1.1145709, 4.554903071), 0.6)
    )
    BATCH_SIZE = 25

    @property
    def ml_model_file(self):
        return str(self.ml_model.path / f'{self.ml_model.name}.pb')

    def calc_embedding(self, face_img: Array3D) -> Array3D:
        return self._calculate_embeddings([face_img])[0]

    @cached_property
    def _embedding_calculator(self):
        with tf1.Graph().as_default() as graph:
            graph_def = tf1.GraphDef()
            with gfile.FastGFile(self.ml_model_file, 'rb') as f:
                model = f.read()
            graph_def.ParseFromString(model)
            tf1.import_graph_def(graph_def, name='')
            return _EmbeddingCalculator(graph=graph, sess=tf1.Session(graph=graph))

    def _calculate_embeddings(self, cropped_images):
        """Run forward pass to calculate embeddings"""
        prewhitened_images = [prewhiten(img) for img in cropped_images]
        calc_model = self._embedding_calculator
        graph_images_placeholder = calc_model.graph.get_tensor_by_name("input:0")
        graph_embeddings = calc_model.graph.get_tensor_by_name("embeddings:0")
        graph_phase_train_placeholder = calc_model.graph.get_tensor_by_name("phase_train:0")
        embedding_size = graph_embeddings.get_shape()[1]
        image_count = len(prewhitened_images)
        batches_per_epoch = int(math.ceil(1.0 * image_count / self.BATCH_SIZE))
        embeddings = np.zeros((image_count, embedding_size))
        for i in range(batches_per_epoch):
            start_index = i * self.BATCH_SIZE
            end_index = min((i + 1) * self.BATCH_SIZE, image_count)
            feed_dict = {graph_images_placeholder: prewhitened_images, graph_phase_train_placeholder: False}
            embeddings[start_index:end_index, :] = calc_model.sess.run(
                graph_embeddings, feed_dict=feed_dict)
        return embeddings


class LandmarksDetector(mixins.LandmarksDetectorMixin, base.BasePlugin):
    """ Extract landmarks from FaceDetector results."""


class PoseEstimator(mixins.PoseEstimatorMixin, base.BasePlugin):
    """ Estimate head rotation regarding the camera """
    
    @staticmethod
    def landmarks_names_ordered():
        """ List of lanmarks names orderred as in detector """
        return FaceDetector.KEYPOINTS_ORDER
