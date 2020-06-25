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
import os
import pickle
import sys

sys.path.append(os.path.join(os.path.dirname(__file__), '..', 'eval'))
import lfw

parser = argparse.ArgumentParser(description='Package LFW images')
# general
parser.add_argument('--data-dir', default='', help='')
parser.add_argument('--image-size', type=str, default='112,96', help='')
parser.add_argument('--output', default='', help='path to save.')
args = parser.parse_args()
lfw_dir = args.data_dir
image_size = [int(x) for x in args.image_size.split(',')]
lfw_pairs = lfw.read_pairs(os.path.join(lfw_dir, 'pairs.txt'))
lfw_paths, issame_list = lfw.get_paths(lfw_dir, lfw_pairs, 'jpg')
lfw_bins = []
# lfw_data = nd.empty((len(lfw_paths), 3, image_size[0], image_size[1]))
i = 0
for path in lfw_paths:
    with open(path, 'rb') as fin:
        _bin = fin.read()
        lfw_bins.append(_bin)
        # img = mx.image.imdecode(_bin)
        # img = nd.transpose(img, axes=(2, 0, 1))
        # lfw_data[i][:] = img
        i += 1
        if i % 1000 == 0:
            print('loading lfw', i)

with open(args.output, 'wb') as f:
    pickle.dump((lfw_bins, issame_list), f, protocol=pickle.HIGHEST_PROTOCOL)
