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

import face_model
import argparse
import cv2
import sys
import numpy as np
import datetime

parser = argparse.ArgumentParser(description='face model test')
# general
parser.add_argument('--image-size', default='112,112', help='')
parser.add_argument('--image', default='Tom_Hanks_54745.png', help='')
parser.add_argument('--model', default='model/model,0', help='path to load model.')
parser.add_argument('--gpu', default=0, type=int, help='gpu id')
parser.add_argument('--det', default=0, type=int, help='mtcnn option, 1 means using R+O, 0 means detect from begining')
args = parser.parse_args()

model = face_model.FaceModel(args)
#img = cv2.imread('Tom_Hanks_54745.png')
img = cv2.imread(args.image)
img = model.get_input(img)
#f1 = model.get_feature(img)
#print(f1[0:10])
for _ in range(5):
  gender, age = model.get_ga(img)
time_now = datetime.datetime.now()
count = 200
for _ in range(count):
  gender, age = model.get_ga(img)
time_now2 = datetime.datetime.now()
diff = time_now2 - time_now
print('time cost', diff.total_seconds()/count)
print('gender is',gender)
print('age is', age)

