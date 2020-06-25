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

import mxnet.optimizer as optimizer
from mxnet import ndarray as nd


class NoiseSGD(optimizer.SGD):
    """Noise SGD.


    This optimizer accepts the same arguments as :class:`.SGD`.
    """

    def __init__(self, scale, **kwargs):
        super(NoiseSGD, self).__init__(**kwargs)
        print('init noise sgd with', scale)
        self.scale = scale

    def update(self, index, weight, grad, state):
        assert (isinstance(weight, NDArray))
        assert (isinstance(grad, NDArray))
        self._update_count(index)
        lr = self._get_lr(index)
        wd = self._get_wd(index)

        grad = grad * self.rescale_grad
        if self.clip_gradient is not None:
            grad = clip(grad, -self.clip_gradient, self.clip_gradient)
        noise = nd.random.normal(scale=self.scale, shape=grad.shape, dtype=grad.dtype, ctx=grad.context)
        grad += noise

        if state is not None:
            mom = state
            mom[:] *= self.momentum
            grad += wd * weight
            mom[:] += grad
            grad[:] += self.momentum * mom
            weight[:] += -lr * grad
        else:
            assert self.momentum == 0.0
            weight[:] += -lr * (grad + wd * weight)
