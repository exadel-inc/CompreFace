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

import argparse
import numpy as np
import mxnet as mx
from config import config
from data import FaceSegIter
from metric import NMEMetric

parser = argparse.ArgumentParser(description='test nme on rec data')
# general
parser.add_argument('--rec', default='./data_2d/ibug.rec', help='rec data path')
parser.add_argument('--prefix', default='', help='model prefix')
parser.add_argument('--epoch', type=int, default=1, help='model epoch')
parser.add_argument('--gpu', type=int, default=0, help='')
parser.add_argument('--landmark-type', default='2d', help='')
parser.add_argument('--image-size', type=int, default=128, help='')
args = parser.parse_args()

rec_path = args.rec
ctx_id = args.gpu
prefix = args.prefix
epoch = args.epoch
image_size = (args.image_size, args.image_size)
config.landmark_type = args.landmark_type
config.input_img_size = image_size[0]

if ctx_id>=0:
  ctx = mx.gpu(ctx_id)
else:
  ctx = mx.cpu()
sym, arg_params, aux_params = mx.model.load_checkpoint(prefix, epoch)
all_layers = sym.get_internals()
sym = all_layers['heatmap_output']
#model = mx.mod.Module(symbol=sym, context=ctx, data_names=['data'], label_names=['softmax_label'])
model = mx.mod.Module(symbol=sym, context=ctx, data_names=['data'], label_names=None)
#model = mx.mod.Module(symbol=sym, context=ctx)
model.bind(for_training=False, data_shapes=[('data', (1, 3, image_size[0], image_size[1]))])
model.set_params(arg_params, aux_params)

val_iter = FaceSegIter(path_imgrec = rec_path,
  batch_size = 1,
  aug_level = 0,
  )
_metric = NMEMetric()
#val_metric = mx.metric.create(_metric)
#val_metric.reset()
#val_iter.reset()
nme = []
for i, eval_batch in enumerate(val_iter):
  if i%10==0:
    print('processing', i)
  #print(eval_batch.data[0].shape, eval_batch.label[0].shape)
  batch_data = mx.io.DataBatch(eval_batch.data)
  model.forward(batch_data, is_train=False)
  #model.update_metric(val_metric, eval_batch.label, True)
  pred_label = model.get_outputs()[-1].asnumpy()
  label = eval_batch.label[0].asnumpy()
  _nme = _metric.cal_nme(label, pred_label)
  nme.append(_nme)
print(np.mean(nme))

