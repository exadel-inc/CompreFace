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

import numpy as np
import mxnet as mx

class AccMetric(mx.metric.EvalMetric):
  def __init__(self):
    self.axis = 1
    super(AccMetric, self).__init__(
        'acc', axis=self.axis,
        output_names=None, label_names=None)
    self.losses = []
    self.count = 0

  def update(self, labels, preds):
    self.count+=1
    label = labels[0]
    pred_label = preds[1]
    #print('ACC', label.shape, pred_label.shape)
    if pred_label.shape != label.shape:
        pred_label = mx.ndarray.argmax(pred_label, axis=self.axis)
    pred_label = pred_label.asnumpy().astype('int32').flatten()
    label = label.asnumpy()
    if label.ndim==2:
      label = label[:,0]
    label = label.astype('int32').flatten()
    assert label.shape==pred_label.shape
    self.sum_metric += (pred_label.flat == label.flat).sum()
    self.num_inst += len(pred_label.flat)

class LossValueMetric(mx.metric.EvalMetric):
  def __init__(self):
    self.axis = 1
    super(LossValueMetric, self).__init__(
        'lossvalue', axis=self.axis,
        output_names=None, label_names=None)
    self.losses = []

  def update(self, labels, preds):
    #label = labels[0].asnumpy()
    pred = preds[-1].asnumpy()
    #print('in loss', pred.shape)
    #print(pred)
    loss = pred[0]
    self.sum_metric += loss
    self.num_inst += 1.0
    #gt_label = preds[-2].asnumpy()
    #print(gt_label)
