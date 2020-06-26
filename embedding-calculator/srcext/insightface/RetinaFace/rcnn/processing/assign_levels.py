#  Version: 2020.02.21
#
#  MIT License
#
#  Copyright (c) 2018 Jiankang Deng and Jia Guo
#
#  Permission is hereby granted, free of charge, to any person obtaining a copy
#  of this software and associated documentation files (the "Software"), to deal
#  in the Software without restriction, including without limitation the rights
#  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#  copies of the Software, and to permit persons to whom the Software is
#  furnished to do so, subject to the following conditions:
#
#  The above copyright notice and this permission notice shall be included in all
#  copies or substantial portions of the Software.
#
#  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#  SOFTWARE.
#

from rcnn.config import config
import numpy as np


def compute_assign_targets(rois, threshold):
    rois_area = np.sqrt((rois[:, 2] - rois[:, 0] + 1) * (rois[:, 3] - rois[:, 1] + 1))
    num_rois = np.shape(rois)[0]
    assign_levels = np.zeros(num_rois, dtype=np.uint8)
    for i, stride in enumerate(config.RCNN_FEAT_STRIDE):
        thd = threshold[i]
        idx = np.logical_and(thd[1] <= rois_area, rois_area < thd[0])
        assign_levels[idx] = stride

    assert 0 not in assign_levels, "All rois should assign to specify levels."
    return assign_levels


def add_assign_targets(roidb):
    """
    given roidb, add ['assign_level']
    :param roidb: roidb to be processed. must have gone through imdb.prepare_roidb
    """
    print 'add assign targets'
    assert len(roidb) > 0
    assert 'boxes' in roidb[0]

    area_threshold = [[np.inf, 448],
                      [448,    224],
                      [224,    112],
                      [112,     0]]

    assert len(config.RCNN_FEAT_STRIDE) == len(area_threshold)

    num_images = len(roidb)
    for im_i in range(num_images):
        rois = roidb[im_i]['boxes']
        roidb[im_i]['assign_levels'] = compute_assign_targets(rois, area_threshold)
