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
import cv2
from typing import List

import tensorflow as tf
import numpy as np
from cached_property import cached_property
from mtcnn_tflite.MTCNN import MTCNN

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
    BOX_MARGIN = 32
    IMAGE_SIZE = 160
    IMG_LENGTH_LIMIT = ENV.IMG_LENGTH_LIMIT

    # detection settings
    det_prob_threshold = 0.85
    det_threshold_a = 0.9436513301
    det_threshold_b = 0.7059968943
    det_threshold_c = 0.5506904359

    @cached_property
    def _face_detection_net(self):
        return MTCNN(
            min_face_size=self.FACE_MIN_SIZE,
            scale_factor=self.SCALE_FACTOR,
            steps_threshold=[self.det_threshold_a, self.det_threshold_b, self.det_threshold_c]
        )

    def crop_face(self, img: Array3D, box: BoundingBoxDTO) -> Array3D:
        return cv2.resize(crop_img(img, box), (self.IMAGE_SIZE, self.IMAGE_SIZE))

    def find_faces(self, img: Array3D, det_prob_threshold: float = None) -> List[BoundingBoxDTO]:
        if det_prob_threshold is None:
            det_prob_threshold = self.det_prob_threshold
        assert 0 <= det_prob_threshold <= 1
        scaler = ImgScaler(self.IMG_LENGTH_LIMIT)
        img = scaler.downscale_img(img)

        fdn = self._face_detection_net
        detect_face_result = fdn.detect_faces(img)
        img_size = np.asarray(img.shape)[0:2]
        bounding_boxes = []

        for face in detect_face_result:
            x, y, w, h = face['box']
            margin_x = w / 8
            margin_y = h / 8
            box = BoundingBoxDTO(
                x_min=int(np.maximum(x - margin_x, 0)),
                y_min=int(np.maximum(y - margin_y, 0)),
                x_max=int(np.minimum(x + w + margin_x, img_size[1])),
                y_max=int(np.minimum(y + h + margin_y, img_size[0])),
                np_landmarks=np.array([list(value) for value in face['keypoints'].values()]),
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
        # converted facenet .tflite model
        ('20180402-114759-edgetpu', '1Uwv8w6Uj5M_xdJI9sjay_wkoFoI_zbjk', (1.1817961, 5.291995557), 0.4),
    )
    BATCH_SIZE = 25
    DELIGATES = 'libedgetpu.so.1'

    @property
    def ml_model_file(self):
        return str(self.ml_model.path / f'{self.ml_model.name}.tflite')

    @cached_property
    def _embedding_calculator_tpu(self):
        delegate_list = tf.lite.experimental.load_delegate(self.DELIGATES)
        model = tf.lite.Interpreter(
                                model_path=self.ml_model_file,
                                experimental_delegates=[delegate_list])
        return model

    @cached_property
    def _embedding_calculator(self):
        model = tf.lite.Interpreter(model_path=self.ml_model_file)
        return model

    def calc_embedding(self, face_img: Array3D, mode='CPU') -> Array3D:
        return self._calculate_embeddings([face_img], mode)[0]

    def _calculate_embeddings(self, cropped_images, mode='CPU'):
        """Run forward pass to calculate embeddings"""
        if mode == 'TPU':
            calc_model = self._embedding_calculator_tpu
        else:
            calc_model = self._embedding_calculator
            cropped_images = [prewhiten(img).astype(np.float32) for img in cropped_images]

        input_details = calc_model.get_input_details()
        input_index = input_details[0]['index']
        input_shape = input_details[0]['shape']
        input_size = tuple(input_shape[1:4])

        output_details = calc_model.get_output_details()
        output_index = output_details[0]['index']
        embedding_size = output_details[0]['shape'][1]

        image_count = len(cropped_images)
        batches_per_epoch = int(math.ceil(1.0 * image_count / self.BATCH_SIZE))
        embeddings = np.zeros((image_count, embedding_size))
        preprocessed_images = np.array([img for img in cropped_images])

        for i in range(batches_per_epoch):
            start_index = i * self.BATCH_SIZE
            end_index = min((i + 1) * self.BATCH_SIZE, image_count)
            calc_model.resize_tensor_input(input_index, (end_index-start_index, input_size[0], input_size[1], input_size[2]))
            calc_model.resize_tensor_input(output_index, (end_index-start_index, embedding_size))
            calc_model.allocate_tensors()
            calc_model.set_tensor(input_index, preprocessed_images[start_index:end_index])
            calc_model.invoke()
            embeddings[start_index:end_index, :] = calc_model.get_tensor(output_index)
        return embeddings


class LandmarksDetector(mixins.LandmarksDetectorMixin, base.BasePlugin):
    """ Extract landmarks from FaceDetector results."""