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

from functools import lru_cache
from typing import Tuple, Union

import numpy as np
import tensorflow as tf

from src.services.imgtools.types import Array3D
from src.services.facescan.plugins import base, managers
from src.services.dto import plugin_result
from srcext.rude_carnie.model import inception_v3, get_checkpoint


def prewhiten(img):
    """ Normalize image."""
    mean = np.mean(img)
    std = np.std(img)
    std_adj = np.maximum(std, 1.0 / np.sqrt(img.size))
    y = np.multiply(np.subtract(img, mean), 1 / std_adj)
    return y


@lru_cache(maxsize=2)
def _get_rude_carnie_model(labels: Tuple, model_dir: str):

    IMAGE_SIZE = managers.plugin_manager.detector.IMAGE_SIZE

    g = tf.Graph()
    with g.as_default():
        sess = tf.Session(config=tf.ConfigProto(allow_soft_placement=True))

        images = tf.placeholder(tf.float32, [None, IMAGE_SIZE, IMAGE_SIZE, 3])
        logits = inception_v3(len(labels), images, 1, False)
        tf.global_variables_initializer()

        model_checkpoint_path, global_step = get_checkpoint(model_dir, None, 'checkpoint')

        saver = tf.train.Saver()
        saver.restore(sess, model_checkpoint_path)
        softmax_output = tf.nn.softmax(logits)

        def get_value(img: Array3D) -> Tuple[Union[str, Tuple], float]:
            img = np.expand_dims(prewhiten(img), 0)
            output = sess.run(softmax_output, feed_dict={images:img})[0]
            best_i = int(np.argmax(output))
            return labels[best_i], output[best_i]
        return get_value


class AgeDetector(base.BasePlugin):
    slug = 'age'
    LABELS = ((0, 2), (4, 6), (8, 12), (15, 20), (25, 32), (38, 43), (48, 53), (60, 100))
    ml_models = (
        ('22801', '1PxK72O-NROEz8pUGDDFRDYF4AABbvWiC'),
    )

    def __call__(self, face_img: Array3D):
        model = _get_rude_carnie_model(self.LABELS, self.ml_model.path)
        value, probability = model(face_img)
        return plugin_result.AgeDTO(age=value, age_probability=probability)


class GenderDetector(base.BasePlugin):
    slug = 'gender'
    LABELS = ('male', 'female')
    ml_models = (
        ('21936', '1j9B76U3b4_F9e8-OKlNdOBQKa2ziGe_-'),
    )

    def __call__(self, face_img: Array3D):
        model = _get_rude_carnie_model(self.LABELS, self.ml_model.path)
        value, probability = model(face_img)
        return plugin_result.GenderDTO(gender=value, gender_probability=probability)
