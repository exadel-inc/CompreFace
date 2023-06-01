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
from cached_property import cached_property

from src.services.imgtools.types import Array3D
from src.services.facescan.plugins import base
from src.services.dto import plugin_result

import cv2


class MaskDetector(base.BasePlugin):
    slug = 'mask'
    LABELS = ('without_mask', 'with_mask', 'mask_weared_incorrect')
    ml_models = (
        ('inception_v3_on_mafa_kaggle123', '1nhmv4Pd8nnV8XHv6vlf6RCpwQLow78zS'),
    )

    INPUT_IMAGE_SIZE = 100

    @property
    def retain_folder_structure(self) -> bool:
        return True

    @cached_property
    def _model(self):
        model = tf2.keras.models.load_model(str(self.ml_model.path))

        def get_value(img: Array3D) -> Tuple[Union[str, Tuple], float]:
            img = cv2.resize(img, dsize=(self.INPUT_IMAGE_SIZE, self.INPUT_IMAGE_SIZE),
                             interpolation=cv2.INTER_CUBIC)
            img = np.expand_dims(img, 0)

            scores = model.predict(img)
            val = self.LABELS[int(np.argmax(scores, axis=1)[0])]
            prob = scores[0][int(np.argmax(scores, axis=1)[0])]
            return val, prob
        return get_value

    def __call__(self, face: plugin_result.FaceDTO):
        value, probability = self._model(face._face_img)
        return plugin_result.MaskDTO(mask=value, mask_probability=probability)


