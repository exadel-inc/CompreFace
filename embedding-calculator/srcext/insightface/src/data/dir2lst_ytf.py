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

from easydict import EasyDict as edict

input_dir = '/raid5data/dplearn/YTF/aligned_images_DB'
ret = []
label = 0
person_names = []
for person_name in os.listdir(input_dir):
    person_names.append(person_name)
person_names = sorted(person_names)
for person_name in person_names:
    _subdir = os.path.join(input_dir, person_name)
    if not os.path.isdir(_subdir):
        continue
    for _subdir2 in os.listdir(_subdir):
        _subdir2 = os.path.join(_subdir, _subdir2)
        if not os.path.isdir(_subdir2):
            continue
        _ret = []
        for img in os.listdir(_subdir2):
            fimage = edict()
            fimage.id = os.path.join(_subdir2, img)
            fimage.classname = str(label)
            fimage.image_path = os.path.join(_subdir2, img)
            fimage.bbox = None
            fimage.landmark = None
            _ret.append(fimage)
        ret += _ret
    label += 1
for item in ret:
    print("%d\t%s\t%d" % (1, item.image_path, int(item.classname)))
