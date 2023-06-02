#!/usr/bin/python3
# -*- coding: utf-8 -*-

# MIT License
#
# Copyright (c) 2019 Iván de Paz Centeno
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

from tensorflow.keras.layers import Input, Dense, Conv2D, MaxPooling2D, PReLU, Flatten, Softmax
from tensorflow.keras.models import Model

import numpy as np


class NetworkFactory:

    def build_pnet(self, input_shape=None):
        if input_shape is None:
            input_shape = (None, None, 3)

        p_inp = Input(input_shape)

        p_layer = Conv2D(10, kernel_size=(3, 3), strides=(1, 1), padding="valid")(p_inp)
        p_layer = PReLU(shared_axes=[1, 2])(p_layer)
        p_layer = MaxPooling2D(pool_size=(2, 2), strides=(2, 2), padding="same")(p_layer)

        p_layer = Conv2D(16, kernel_size=(3, 3), strides=(1, 1), padding="valid")(p_layer)
        p_layer = PReLU(shared_axes=[1, 2])(p_layer)

        p_layer = Conv2D(32, kernel_size=(3, 3), strides=(1, 1), padding="valid")(p_layer)
        p_layer = PReLU(shared_axes=[1, 2])(p_layer)

        p_layer_out1 = Conv2D(2, kernel_size=(1, 1), strides=(1, 1))(p_layer)
        p_layer_out1 = Softmax(axis=3)(p_layer_out1)

        p_layer_out2 = Conv2D(4, kernel_size=(1, 1), strides=(1, 1))(p_layer)

        p_net = Model(p_inp, [p_layer_out2, p_layer_out1])

        return p_net

    def build_rnet(self, input_shape=None):
        if input_shape is None:
            input_shape = (24, 24, 3)

        r_inp = Input(input_shape)

        r_layer = Conv2D(28, kernel_size=(3, 3), strides=(1, 1), padding="valid")(r_inp)
        r_layer = PReLU(shared_axes=[1, 2])(r_layer)
        r_layer = MaxPooling2D(pool_size=(3, 3), strides=(2, 2), padding="same")(r_layer)

        r_layer = Conv2D(48, kernel_size=(3, 3), strides=(1, 1), padding="valid")(r_layer)
        r_layer = PReLU(shared_axes=[1, 2])(r_layer)
        r_layer = MaxPooling2D(pool_size=(3, 3), strides=(2, 2), padding="valid")(r_layer)

        r_layer = Conv2D(64, kernel_size=(2, 2), strides=(1, 1), padding="valid")(r_layer)
        r_layer = PReLU(shared_axes=[1, 2])(r_layer)
        r_layer = Flatten()(r_layer)
        r_layer = Dense(128)(r_layer)
        r_layer = PReLU()(r_layer)

        r_layer_out1 = Dense(2)(r_layer)
        r_layer_out1 = Softmax(axis=1)(r_layer_out1)

        r_layer_out2 = Dense(4)(r_layer)

        r_net = Model(r_inp, [r_layer_out2, r_layer_out1])

        return r_net

    def build_onet(self, input_shape=None):
        if input_shape is None:
            input_shape = (48, 48, 3)

        o_inp = Input(input_shape)
        o_layer = Conv2D(32, kernel_size=(3, 3), strides=(1, 1), padding="valid")(o_inp)
        o_layer = PReLU(shared_axes=[1, 2])(o_layer)
        o_layer = MaxPooling2D(pool_size=(3, 3), strides=(2, 2), padding="same")(o_layer)

        o_layer = Conv2D(64, kernel_size=(3, 3), strides=(1, 1), padding="valid")(o_layer)
        o_layer = PReLU(shared_axes=[1, 2])(o_layer)
        o_layer = MaxPooling2D(pool_size=(3, 3), strides=(2, 2), padding="valid")(o_layer)

        o_layer = Conv2D(64, kernel_size=(3, 3), strides=(1, 1), padding="valid")(o_layer)
        o_layer = PReLU(shared_axes=[1, 2])(o_layer)
        o_layer = MaxPooling2D(pool_size=(2, 2), strides=(2, 2), padding="same")(o_layer)

        o_layer = Conv2D(128, kernel_size=(2, 2), strides=(1, 1), padding="valid")(o_layer)
        o_layer = PReLU(shared_axes=[1, 2])(o_layer)

        o_layer = Flatten()(o_layer)
        o_layer = Dense(256)(o_layer)
        o_layer = PReLU()(o_layer)

        o_layer_out1 = Dense(2)(o_layer)
        o_layer_out1 = Softmax(axis=1)(o_layer_out1)
        o_layer_out2 = Dense(4)(o_layer)
        o_layer_out3 = Dense(10)(o_layer)

        o_net = Model(o_inp, [o_layer_out2, o_layer_out3, o_layer_out1])
        return o_net

    def build_P_R_O_nets_from_file(self, weights_file):
        weights = np.load(weights_file, allow_pickle=True).tolist()

        p_net = self.build_pnet()
        r_net = self.build_rnet()
        o_net = self.build_onet()

        p_net.set_weights(weights['pnet'])
        r_net.set_weights(weights['rnet'])
        o_net.set_weights(weights['onet'])

        return p_net, r_net, o_net
