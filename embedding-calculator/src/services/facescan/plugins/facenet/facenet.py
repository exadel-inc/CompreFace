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
import tensorflow as tf
from tensorflow.python.platform import gfile
from cached_property import cached_property
from facenet.src.align import detect_face

from src.constants import ENV
from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.facescan.plugins import mixins
from src.services.facescan.imgscaler.imgscaler import ImgScaler
from src.services.imgtools.proc_img import crop_img, squish_img
from src.services.imgtools.types import Array3D
from src.services.utils.pyutils import get_current_dir

from src.services.facescan.plugins import base

CURRENT_DIR = get_current_dir(__file__)

logger = logging.getLogger(__name__)
_EmbeddingCalculator = namedtuple('_EmbeddingCalculator', 'graph sess')
_FaceDetectionNets = namedtuple('_FaceDetectionNets', 'pnet rnet onet')


class FaceDetector(mixins.FaceDetectorMixin, base.BasePlugin):
    FACE_MIN_SIZE = 20
    SCALE_FACTOR = 0.709
    BOX_MARGIN = 32
    IMAGE_SIZE = 160
    IMG_LENGTH_LIMIT = ENV.IMG_LENGTH_LIMIT

    # detection settings
    det_prob_threshold = 0.65
    det_threshold_a = 0.9436513301
    det_threshold_b = 0.7059968943
    det_threshold_c = 0.5506904359

    @cached_property
    def _face_detection_nets(self):
        with tf.Graph().as_default():
            sess = tf.Session()
            return _FaceDetectionNets(*detect_face.create_mtcnn(sess, None))

    def crop_face(self, img: Array3D, box: BoundingBoxDTO) -> Array3D:
        return squish_img(crop_img(img, box), (self.IMAGE_SIZE, self.IMAGE_SIZE))

    def find_faces(self, img: Array3D, det_prob_threshold: float = None) -> List[BoundingBoxDTO]:
        if det_prob_threshold is None:
            det_prob_threshold = self.det_prob_threshold
        assert 0 <= det_prob_threshold <= 1
        scaler = ImgScaler(self.IMG_LENGTH_LIMIT)
        img = scaler.downscale_img(img)

        fdn = self._face_detection_nets
        detect_face_result = detect_face.detect_face(
            img, self.FACE_MIN_SIZE, fdn.pnet, fdn.rnet, fdn.onet,
            [self.det_threshold_a, self.det_threshold_b, self.det_threshold_c],
            self.SCALE_FACTOR)
        img_size = np.asarray(img.shape)[0:2]
        bounding_boxes = []

        detect_face_result = list(
            zip(detect_face_result[0], detect_face_result[1].transpose()))
        for result_item, landmarks in detect_face_result:
            result_item = np.squeeze(result_item)
            margin = self.BOX_MARGIN / 2
            box = BoundingBoxDTO(
                x_min=int(np.maximum(result_item[0] - margin, 0)),
                y_min=int(np.maximum(result_item[1] - margin, 0)),
                x_max=int(np.minimum(result_item[2] + margin, img_size[1])),
                y_max=int(np.minimum(result_item[3] + margin, img_size[0])),
                np_landmarks=landmarks.reshape(2, 5).transpose(),
                probability=result_item[4]
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
        ('20180402-114759', '1im5Qq006ZEV_tViKh3cgia_Q4jJ13bRK'),
        # CASIA-WebFace training set, 0.9905 LFW accuracy
        ('20180408-102900', '100w4JIUz44Tkwte9F-wEH0DOFsY-bPaw'),
    )
    BATCH_SIZE = 25
    DIFFERENCE_THRESHOLD = 0.2

    @property
    def ml_model_file(self):
        return str(self.ml_model.path / f'{self.ml_model.name}.pb')

    def calc_embedding(self, face_img: Array3D) -> Array3D:
        return self._calculate_embeddings([face_img])[0]

    @cached_property
    def _embedding_calculator(self):
        with tf.Graph().as_default() as graph:
            graph_def = tf.GraphDef()
            with gfile.FastGFile(self.ml_model_file, 'rb') as f:
                model = f.read()
            graph_def.ParseFromString(model)
            tf.import_graph_def(graph_def, name='')
            return _EmbeddingCalculator(graph=graph, sess=tf.Session(graph=graph))

    def _calculate_embeddings(self, cropped_images):
        """Run forward pass to calculate embeddings"""
        calc_model = self._embedding_calculator
        graph_images_placeholder = calc_model.graph.get_tensor_by_name("input:0")
        graph_embeddings = calc_model.graph.get_tensor_by_name("embeddings:0")
        graph_phase_train_placeholder = calc_model.graph.get_tensor_by_name("phase_train:0")
        embedding_size = graph_embeddings.get_shape()[1]
        image_count = len(cropped_images)
        batches_per_epoch = int(math.ceil(1.0 * image_count / self.BATCH_SIZE))
        embeddings = np.zeros((image_count, embedding_size))
        for i in range(batches_per_epoch):
            start_index = i * self.BATCH_SIZE
            end_index = min((i + 1) * self.BATCH_SIZE, image_count)
            feed_dict = {graph_images_placeholder: cropped_images, graph_phase_train_placeholder: False}
            embeddings[start_index:end_index, :] = calc_model.sess.run(
                graph_embeddings, feed_dict=feed_dict)
        return embeddings


class LandmarksDetector(mixins.LandmarksDetectorMixin, base.BasePlugin):
    """ Extract landmarks from FaceDetector results."""
