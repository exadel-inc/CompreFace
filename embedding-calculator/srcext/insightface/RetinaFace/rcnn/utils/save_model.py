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

import mxnet as mx


def save_checkpoint(prefix, epoch, arg_params, aux_params):
    """Checkpoint the model data into file.
    :param prefix: Prefix of model name.
    :param epoch: The epoch number of the model.
    :param arg_params: dict of str to NDArray
        Model parameter, dict of name to NDArray of net's weights.
    :param aux_params: dict of str to NDArray
        Model parameter, dict of name to NDArray of net's auxiliary states.
    :return: None
    prefix-epoch.params will be saved for parameters.
    """
    save_dict = {('arg:%s' % k) : v for k, v in arg_params.items()}
    save_dict.update({('aux:%s' % k) : v for k, v in aux_params.items()})
    param_name = '%s-%04d.params' % (prefix, epoch)
    mx.nd.save(param_name, save_dict)
