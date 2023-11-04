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

from typing import Tuple
import numpy as np
import cv2
from skimage import transform as trans

from src.constants import ENV


if ENV.RUN_MODE:
    import mxnet as mx


def predict_landmark2d106(model, img,
                          crop_size:  Tuple[int, int],
                          box_center: Tuple[int, int],
                          box_size: Tuple[int, int]):
    rotate = 0
    _scale = crop_size[0] * 2 / 3.0 / max(box_size)
    rimg, M = transform(img, box_center, crop_size[0], _scale, rotate)

    input_blob = np.zeros((1, 3) + crop_size, dtype=np.float32)
    input_blob[0] = np.transpose(rimg, (2, 0, 1))  # 3*112*112, RGB

    data = mx.nd.array(input_blob)
    db = mx.io.DataBatch(data=(data,))
    model.forward(db, is_train=False)
    pred = model.get_outputs()[-1].asnumpy()[0].reshape((-1, 2))
    pred[:, 0:2] += 1
    pred[:, 0:2] *= (crop_size[0] // 2)

    IM = cv2.invertAffineTransform(M)
    return trans_points2d(pred, IM)


def transform(data, center, output_size, scale, rotation):
    scale_ratio = scale
    rot = float(rotation) * np.pi / 180.0
    t1 = trans.SimilarityTransform(scale=scale_ratio)
    cx = center[0] * scale_ratio
    cy = center[1] * scale_ratio
    t2 = trans.SimilarityTransform(translation=(-1 * cx, -1 * cy))
    t3 = trans.SimilarityTransform(rotation=rot)
    t4 = trans.SimilarityTransform(translation=(output_size / 2, output_size / 2))
    t = t1 + t2 + t3 + t4
    M = t.params[0:2]
    cropped = cv2.warpAffine(data,
                             M, (output_size, output_size),
                             borderValue=0.0)
    return cropped, M


def trans_points2d(pts, M):
    new_pts = np.zeros(shape=pts.shape, dtype=np.float32)
    for i in range(pts.shape[0]):
        pt = pts[i]
        new_pt = np.array([pt[0], pt[1], 1.], dtype=np.float32)
        new_pt = np.dot(M, new_pt)
        #print('new_pt', new_pt.shape, new_pt)
        new_pts[i] = new_pt[0:2]
    return new_pts