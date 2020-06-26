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

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import os

import cv2
import numpy as np

pairs_file = '/raid5data/dplearn/YTF/splits2.txt'
stat = [0, 0]
for line in open(pairs_file, 'r'):
    line = line.strip()
    if line.startswith('split'):
        continue
    vec = line.split(',')
    issame = int(vec[-1])
    if issame:
        stat[0] += 1
    else:
        stat[1] += 1
print('stat', stat)

image_dir = '/raid5data/dplearn/YTF/images'


def get_img(name, vid):
    input_dir = os.path.join(image_dir, name, str(vid))
    paths = []
    for img in os.listdir(input_dir):
        path = os.path.join(input_dir, img)
        paths.append(path)
    paths = sorted(paths)
    parts = 8
    assert len(paths) >= parts
    gap = len(paths) // parts
    img = None
    for i in xrange(parts):
        idx = gap * i
        path = paths[idx]
        _img = cv2.imread(path)
        # print(_img.shape)
        if img is None:
            img = _img
        else:
            img = np.concatenate((img, _img), axis=1)
    return img


text_color = (153, 255, 51)
for input in ['ytf_false_positive', 'ytf_false_negative']:
    all_img = None
    pp = 0
    for line in open(input + ".log", 'r'):
        if line.startswith("\t"):
            break
        vec = line.strip().split(',')
        img1 = get_img(vec[0], int(vec[1]))
        img2 = get_img(vec[2], int(vec[3]))
        img = np.concatenate((img1, img2), axis=0)
        if all_img is None:
            all_img = img
        else:
            all_img = np.concatenate((all_img, img), axis=0)
        blank_img = np.zeros((20, 112 * 8, 3), dtype=np.uint8)
        blank_img[:, :, :] = 255
        font = cv2.FONT_HERSHEY_SIMPLEX
        k = "centre-distance:%.3f" % (float(vec[4]))
        # print(k)
        cv2.putText(blank_img, k, (350, blank_img.shape[0] - 4), font, 0.6, text_color, 2)
        all_img = np.concatenate((all_img, blank_img), axis=0)
        pp += 1

    filename = os.path.join('badcases', input + ".png")
    cv2.imwrite(filename, all_img)
