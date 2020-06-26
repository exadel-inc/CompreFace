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
from easydict import EasyDict as edict

config = edict()

#default training/dataset config
config.num_classes = 3
config.input_img_size = 256
config.output_label_size = 64

# network settings
network = edict()

network.hourglass = edict()
network.hourglass.net_sta = 0
network.hourglass.net_n = 4
network.hourglass.net_dcn = 0
network.hourglass.net_stacks = 1
network.hourglass.net_block = 'resnet'
network.hourglass.net_binarize = False
network.hourglass.losstype = 'heatmap'
network.hourglass.multiplier = 1.0

network.prnet = edict()
network.prnet.net_sta = 0
network.prnet.net_n = 5
network.prnet.net_dcn = 0
network.prnet.net_stacks = 1
network.prnet.net_modules = 2
network.prnet.net_block = 'hpm'
network.prnet.net_binarize = False
network.prnet.losstype = 'heatmap'
network.prnet.multiplier = 0.25

network.hpm = edict()
network.hpm.net_sta = 0
network.hpm.net_n = 4
network.hpm.net_dcn = 0
network.hpm.net_stacks = 1
network.hpm.net_block = 'hpm'
network.hpm.net_binarize = False
network.hpm.losstype = 'heatmap'
network.hpm.multiplier = 1.0


# dataset settings
dataset = edict()


dataset.prnet = edict()
dataset.prnet.dataset = '3D'
dataset.prnet.landmark_type = 'dense'
dataset.prnet.dataset_path = './data64'
dataset.prnet.num_classes = 3
dataset.prnet.input_img_size = 256
dataset.prnet.output_label_size = 64
#dataset.prnet.label_xfirst = False
dataset.prnet.val_targets = ['']

# default settings
default = edict()

# default network
default.network = 'hpm'
default.pretrained = ''
default.pretrained_epoch = 0
# default dataset
default.dataset = 'prnet'
default.frequent = 20
default.verbose = 200
default.kvstore = 'device'

default.prefix = 'model/A'
default.end_epoch = 10000
default.lr = 0.00025
default.wd = 0.0
default.per_batch_size = 20
default.lr_step = '16000,24000,30000'

def generate_config(_network, _dataset):
    for k, v in network[_network].items():
      config[k] = v
      default[k] = v
    for k, v in dataset[_dataset].items():
      config[k] = v
      default[k] = v
    config.network = _network
    config.dataset = _dataset

