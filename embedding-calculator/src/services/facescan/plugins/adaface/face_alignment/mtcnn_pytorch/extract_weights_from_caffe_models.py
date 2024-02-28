import caffe
import numpy as np

"""
The purpose of this script is to convert pretrained weights taken from
official implementation here:
https://github.com/kpzhang93/MTCNN_face_detection_alignment/tree/master/code/codes/MTCNNv2
to required format.

In a nutshell, it just renames and transposes some of the weights.
You don't have to use this script because weights are already in `src/weights`.
"""


def get_all_weights(net):
    all_weights = {}
    for p in net.params:
        if 'conv' in p:
            name = 'features.' + p
            if '-' in p:
                s = list(p)
                s[-2] = '_'
                s = ''.join(s)
                all_weights[s + '.weight'] = net.params[p][0].data
                all_weights[s + '.bias'] = net.params[p][1].data
            elif len(net.params[p][0].data.shape) == 4:
                all_weights[name + '.weight'] = net.params[p][0].data.transpose((0, 1, 3, 2))
                all_weights[name + '.bias'] = net.params[p][1].data
            else:
                all_weights[name + '.weight'] = net.params[p][0].data
                all_weights[name + '.bias'] = net.params[p][1].data
        elif 'prelu' in p.lower():
            all_weights['features.' + p.lower() + '.weight'] = net.params[p][0].data
    return all_weights


# P-Net
net = caffe.Net('caffe_models/det1.prototxt', 'caffe_models/det1.caffemodel', caffe.TEST)
np.save('src/weights/pnet.npy', get_all_weights(net))

# R-Net
net = caffe.Net('caffe_models/det2.prototxt', 'caffe_models/det2.caffemodel', caffe.TEST)
np.save('src/weights/rnet.npy', get_all_weights(net))

# O-Net
net = caffe.Net('caffe_models/det3.prototxt', 'caffe_models/det3.caffemodel', caffe.TEST)
np.save('src/weights/onet.npy', get_all_weights(net))
