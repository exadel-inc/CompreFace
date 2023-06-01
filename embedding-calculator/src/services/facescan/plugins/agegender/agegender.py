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
import tensorflow.compat.v1 as tf1
from cached_property import cached_property

from src.services.imgtools.types import Array3D
from src.services.facescan.plugins import base, managers
from src.services.facescan.plugins.agegender import helpers
from src.services.dto import plugin_result


class BaseAgeGender(base.BasePlugin):
    LABELS: Tuple[Tuple[int, int], ...]

    @cached_property
    def _model(self):
        labels = self.LABELS
        model_dir = self.ml_model.path
        IMAGE_SIZE = managers.plugin_manager.detector.IMAGE_SIZE

        g = tf1.Graph()
        with g.as_default():
            sess = tf1.Session(config=tf1.ConfigProto(allow_soft_placement=True))

            images = tf1.placeholder(tf1.float32, [None, IMAGE_SIZE, IMAGE_SIZE, 3])
            logits = helpers.inception_v3(len(labels), images)
            tf1.global_variables_initializer()

            checkpoint = tf1.train.get_checkpoint_state(model_dir)
            saver = tf1.train.Saver()
            saver.restore(sess, checkpoint.model_checkpoint_path)
            softmax_output = tf1.nn.softmax(logits)

            def get_value(img: Array3D) -> Tuple[Union[str, Tuple], float]:
                img = np.expand_dims(helpers.prewhiten(img), 0)
                output = sess.run(softmax_output, feed_dict={images: img})[0]
                best_i = int(np.argmax(output))
                return labels[best_i], output[best_i]
            return get_value


class AgeDetector(BaseAgeGender):
    slug = 'age'
    LABELS = ((0, 2), (4, 6), (8, 12), (15, 20), (25, 32), (38, 43), (48, 53), (60, 100))
    ml_models = (
        ('22801', '1PxK72O-NROEz8pUGDDFRDYF4AABbvWiC'),
    )

    def __call__(self, face: plugin_result.FaceDTO):
        value, probability = self._model(face._face_img)
        return plugin_result.AgeDTO(age=value, age_probability=probability)


class GenderDetector(BaseAgeGender):
    slug = 'gender'
    LABELS = ('male', 'female')
    ml_models = (
        ('21936', '1j9B76U3b4_F9e8-OKlNdOBQKa2ziGe_-'),
    )

    def __call__(self, face: plugin_result.FaceDTO):
        value, probability = self._model(face._face_img)
        return plugin_result.GenderDTO(gender=value, gender_probability=probability)

