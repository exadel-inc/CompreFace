# coding: utf-8

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

# pylint: disable=wrong-import-position
"""InsightFace: A Face Analysis Toolkit."""
from __future__ import absolute_import

# mxnet version check
#mx_version = '1.4.0'
try:
    import mxnet as mx
    #from distutils.version import LooseVersion
    #if LooseVersion(mx.__version__) < LooseVersion(mx_version):
    #    msg = (
    #        "Legacy mxnet-mkl=={} detected, some new modules may not work properly. "
    #        "mxnet-mkl>={} is required. You can use pip to upgrade mxnet "
    #        "`pip install mxnet-mkl --pre --upgrade` "
    #        "or `pip install mxnet-cu90mkl --pre --upgrade`").format(mx.__version__, mx_version)
    #    raise ImportError(msg)
except ImportError:
    raise ImportError(
        "Unable to import dependency mxnet. "
        "A quick tip is to install via `pip install mxnet-mkl/mxnet-cu90mkl --pre`. ")

__version__ = '0.1.5'

from . import model_zoo
from . import utils
from . import app

