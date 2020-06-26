
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

import cv2
import numpy as np
from skimage import transform as trans

src1 = np.array([
     [51.642,50.115],
     [57.617,49.990],
     [35.740,69.007],
     [51.157,89.050],
     [57.025,89.702]], dtype=np.float32)
#<--left 
src2 = np.array([
    [45.031,50.118],
    [65.568,50.872],
    [39.677,68.111],
    [45.177,86.190],
    [64.246,86.758]], dtype=np.float32)

#---frontal
src3 = np.array([
    [39.730,51.138],
    [72.270,51.138],
    [56.000,68.493],
    [42.463,87.010],
    [69.537,87.010]], dtype=np.float32)

#-->right
src4 = np.array([
    [46.845,50.872],
    [67.382,50.118],
    [72.737,68.111],
    [48.167,86.758],
    [67.236,86.190]], dtype=np.float32)

#-->right profile
src5 = np.array([
    [54.796,49.990],
    [60.771,50.115],
    [76.673,69.007],
    [55.388,89.702],
    [61.257,89.050]], dtype=np.float32)

src = np.array([src1,src2,src3,src4,src5])
src_map = {112 : src, 224 : src*2}

arcface_src = np.array([
  [38.2946, 51.6963],
  [73.5318, 51.5014],
  [56.0252, 71.7366],
  [41.5493, 92.3655],
  [70.7299, 92.2041] ], dtype=np.float32 )

arcface_src = np.expand_dims(arcface_src, axis=0)

# In[66]:

# lmk is prediction; src is template
def estimate_norm(lmk, image_size = 112, mode='arcface'):
  assert lmk.shape==(5,2)
  tform = trans.SimilarityTransform()
  lmk_tran = np.insert(lmk, 2, values=np.ones(5), axis=1)
  min_M = []
  min_index = []
  min_error = float('inf') 
  if mode=='arcface':
    assert image_size==112
    src = arcface_src
  else:
    src = src_map[image_size]
  for i in np.arange(src.shape[0]):
    tform.estimate(lmk, src[i])
    M = tform.params[0:2,:]
    results = np.dot(M, lmk_tran.T)
    results = results.T
    error = np.sum(np.sqrt(np.sum((results - src[i]) ** 2,axis=1)))
#         print(error)
    if error< min_error:
        min_error = error
        min_M = M
        min_index = i
  return min_M, min_index

def norm_crop(img, landmark, image_size=112, mode='arcface'):
  M, pose_index = estimate_norm(landmark, image_size, mode)
  warped = cv2.warpAffine(img,M, (image_size, image_size), borderValue = 0.0)
  return warped

