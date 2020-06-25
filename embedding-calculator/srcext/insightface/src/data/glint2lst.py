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

import os
import sys

import numpy as np

input_dir = sys.argv[1]
targets = sys.argv[2]
targets = targets.strip().split(',')
lmap = {}

for ds in targets:
    # image_dir = os.path.join(input_dir, ds)
    lmk_file = os.path.join(input_dir, "%s_lmk" % (ds))
    if not os.path.exists(lmk_file):
        lmk_file = os.path.join(input_dir, "%s_lmk.txt" % (ds))
        if not os.path.exists(lmk_file):
            continue
    # print(ds)
    idx = 0
    for line in open(lmk_file, 'r'):
        idx += 1
        vec = line.strip().split(' ')
        assert len(vec) == 12 or len(vec) == 11
        image_file = os.path.join(input_dir, vec[0])
        assert image_file.endswith('.jpg')
        vlabel = -1  # test mode
        if len(vec) == 12:
            label = int(vec[1])
            if label in lmap:
                vlabel = lmap[label]
            else:
                vlabel = len(lmap)
                lmap[label] = vlabel
            lmk = np.array([float(x) for x in vec[2:]], dtype=np.float32)
        else:
            lmk = np.array([float(x) for x in vec[1:]], dtype=np.float32)
        lmk = lmk.reshape((5, 2)).T
        lmk_str = "\t".join([str(x) for x in lmk.flatten()])
        print("0\t%s\t%d\t0\t0\t0\t0\t%s" % (image_file, vlabel, lmk_str))
        # if idx>10:
        #  break
