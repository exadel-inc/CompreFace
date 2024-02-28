
from __future__ import print_function
import sys
import mxnet as mx
import numpy as np
#from distutils.util import strtobool


class FP16GradOperator(mx.operator.CustomOp):
    def __init__(self, fp16_scale):
        super(FP16GradOperator, self).__init__()
        self.fp16_scale = float(fp16_scale)
        print('XXXX', self.fp16_scale, self.fp16_scale.__class__)

    def forward(self, is_train, req, in_data, out_data, aux):

        for i in range(len(in_data)):
            self.assign(out_data[i], req[i], in_data[i])


    def backward(self, req, out_grad, in_data, out_data, in_grad, aux):
        for i in range(len(in_grad)):
            self.assign(in_grad[i], req[i], out_grad[i]*self.fp16_scale)


@mx.operator.register('fp16_grad')
class FP16GradProp(mx.operator.CustomOpProp):
    def __init__(self, fp16_scale=16.0):
        super(FP16GradProp, self).__init__(need_top_grad=True)
        self.fp16_scale = fp16_scale

    def list_arguments(self):
        return ['data']

    def list_outputs(self):
        return ['data_output']

    def infer_shape(self, in_shape):

        return in_shape, in_shape

    def create_operator(self, ctx, shapes, dtypes):
        return FP16GradOperator(self.fp16_scale)

    #def declare_backward_dependency(self, out_grad, in_data, out_data):
    #    return []


