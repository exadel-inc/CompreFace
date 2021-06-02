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
import tensorflow as tf2
from tensorflow.keras.models import load_model
from cached_property import cached_property

from src.services.imgtools.types import Array3D
from src.services.facescan.plugins import base
from src.services.dto import plugin_result

import cv2


class MaskDetector(base.BasePlugin):
    slug = 'mask'
    LABELS = ('without_mask', 'with_mask', 'mask_weared_incorrect')
    ml_models = (
        ('face_mask_detector', '1AeSYb_E_3cqZM67qXnJ__wJDgw6yqTDV'),
    )
    INPUT_IMAGE_SIZE = 100
    category2label = {0: 'without_mask', 1: 'with_mask', 2: 'mask_weared_incorrect'}

    @cached_property
    def _model(self):
        model = tf2.keras.models.load_model(
            self.ml_model.path,
            options=tf2.saved_model.LoadOptions(
                experimental_io_device='/job:localhost'
            )
        )

        def get_value(img: Array3D) -> Tuple[Union[str, Tuple], float]:
            img = cv2.resize(img, dsize=(self.INPUT_IMAGE_SIZE, self.INPUT_IMAGE_SIZE),
                             interpolation=cv2.INTER_CUBIC)
            img = img[:, :, [2, 1, 0]]
            img = np.expand_dims(img, 0)

            scores = model.predict(img)
            print('Predictions: ' + str(scores))
            val = self.LABELS[int(np.argmax(scores, axis=1)[0])]
            prob = scores[0][int(np.argmax(scores, axis=1)[0])]
            return val, prob
        return get_value

    def __call__(self, face: plugin_result.FaceDTO):
        value, probability = self._model(face._face_img)
        return plugin_result.MaskDTO(mask=value, mask_probability=probability)


