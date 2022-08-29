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

import os
from pathlib import Path
from typing import Tuple, Union
from cached_property import cached_property

import numpy as np

from src.services.dto import plugin_result
from src.services.imgtools.types import Array3D
from src.services.facescan.plugins import base
from src.services.facescan.plugins.insightface.insightface import InsightFaceMixin
from src.constants import ENV

if ENV.RUN_MODE:
    import mxnet as mx
    from mxnet.gluon.model_zoo import vision
    from mxnet.gluon.data.vision import transforms


class MaskDetector(InsightFaceMixin, base.BasePlugin):
    slug = 'mask'
    LABELS = ('without_mask', 'with_mask', 'mask_weared_incorrect')
    ml_models = (
        ('mobilenet_v2_on_mafa_kaggle123', '1DYUIroNXkuYKQypYtCxQvAItLnrTTt5E'),
        ('resnet18_on_mafa_kaggle123', '1A3fNrvgrJqMw54cWRj47LNFNnFvTjmdj')
    )
    if ENV.RUN_MODE:
        img_transforms = transforms.Compose([
            transforms.Resize(224),
            transforms.ToTensor(),
            transforms.Normalize((0.485, 0.456, 0.406), (0.229, 0.224, 0.225))
        ])

    @property
    def input_image_size(self) -> Tuple[int, int]:
        return 224, 224

    @property
    def retain_folder_structure(self) -> bool:
        return True

    @cached_property
    def _model(self):
        gpu_count = mx.context.num_gpus()
        ctx = mx.gpu() if gpu_count > 0 else mx.cpu()

        if self.ml_model_name and self.ml_model_name.split('_')[0] == 'resnet18':
            model = vision.resnet18_v1(classes=len(self.LABELS), ctx=ctx)
        else:
            model = vision.mobilenet_v2_1_0(classes=len(self.LABELS), ctx=ctx)
        model_path = Path(self.ml_model.path) / Path(os.listdir(self.ml_model.path)[0])
        model.load_parameters(str(model_path), ctx=ctx)

        def get_value(img: Array3D) -> Tuple[Union[str, Tuple], float]:
            data = img.reshape((1,) + img.shape)
            data = mx.nd.array(data)

            scores = model(mx.nd.array(self.img_transforms(data), ctx=ctx)).softmax().asnumpy()
            val = self.LABELS[int(np.argmax(scores, axis=1)[0])]
            prob = scores[0][int(np.argmax(scores, axis=1)[0])]
            return val, prob
        return get_value

    def __call__(self, face: plugin_result.FaceDTO):
        value, probability = self._model(face._face_img)
        return plugin_result.MaskDTO(mask=value, mask_probability=probability)