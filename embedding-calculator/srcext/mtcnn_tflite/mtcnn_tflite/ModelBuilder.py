# MIT License
#
# Copyright (c) 2021 CDL Digidow <https://digidow.eu>
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



import numpy as np
import math

from tensorflow.keras.layers import Input, Dense, Conv2D, MaxPooling2D, PReLU, Flatten, Softmax
from tensorflow.keras.models import Model
import tensorflow as tf
from fcache.cache import FileCache
import os
import mtcnn_tflite

class ModelBuilder:
    def __init__(self, min_face_size=20, scale_factor=0.709):
        self.min_face_size = min_face_size
        self.scale_factor = scale_factor

        self.cache = FileCache('mtcnn-tflite-models')
        data_path = os.path.join(os.path.dirname(mtcnn_tflite.__file__), "data")
        self.weights_file = os.path.join(data_path, "mtcnn_weights.npy")
        delegate_list = tf.lite.experimental.load_delegate('libedgetpu.so.1') 
        
        if "r_net" not in self.cache:
            r_net = self.build_rnet()
            converter = tf.lite.TFLiteConverter.from_keras_model(r_net)
            converter.optimizations = [tf.lite.Optimize.DEFAULT]

            r_net = converter.convert()
            self.cache["r_net"] = r_net
        
        self.r_net = tf.lite.Interpreter(model_content=self.cache["r_net"], experimental_delegates = [delegate_list])

        if "o_net" not in self.cache:
            o_net = self.build_onet()
            converter = tf.lite.TFLiteConverter.from_keras_model(o_net)
            converter.optimizations = [tf.lite.Optimize.DEFAULT]

            o_net = converter.convert()
            self.cache["o_net"] = o_net
        
        self.o_net = tf.lite.Interpreter(model_content=self.cache["o_net"], experimental_delegates = [delegate_list])

        self.cache.sync()

    def get_networks(self):
        return (self.p_nets, self.r_net, self.o_net)

    def get_r_o_networks(self):
        return (self.r_net, self.o_net)

    def clear_cache(self):
        self.cache.clear()

    def create_pnet(self, image_dimension):
        img_width, img_height = image_dimension
        scales = self.get_scales(self.min_face_size, img_width, img_height, self.scale_factor)
        delegate_list = tf.lite.experimental.load_delegate('libedgetpu.so.1') #
        if str(image_dimension) not in self.cache:
            ctr = 0
            p_nets = []
            for scale in scales:
                p_net = self.build_pnet((math.ceil(img_height*scale), math.ceil(img_width*scale), 3))
                converter = tf.lite.TFLiteConverter.from_keras_model(p_net)
                converter.optimizations = [tf.lite.Optimize.DEFAULT]
                tflite_model = converter.convert()
                p_nets.append(tflite_model)
            self.cache[str(image_dimension)] = p_nets
            self.cache.sync()

        self.p_nets = []
        for p_net in self.cache[str(image_dimension)]:            
            self.p_nets.append(tf.lite.Interpreter(model_content=p_net, experimental_delegates = [delegate_list]))

        return self.p_nets

 
    def build_pnet(self, input_shape):
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

        weights = np.load(self.weights_file, allow_pickle=True).tolist()
        p_net.set_weights(weights['pnet'])

        return p_net

    def build_rnet(self):
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

        weights = np.load(self.weights_file, allow_pickle=True).tolist()
        r_net.set_weights(weights['rnet'])

        return r_net

    def build_onet(self):
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

        weights = np.load(self.weights_file, allow_pickle=True).tolist()
        o_net.set_weights(weights['onet'])

        return o_net

    def get_scales(self, min_face_size, img_width, img_height, scale_factor):
        m = 12 / min_face_size
        min_layer = np.amin([img_height, img_width]) * m
        scales = []
        factor_count = 0

        while min_layer >= 12:
            scales += [m * np.power(scale_factor, factor_count)]
            min_layer = min_layer * scale_factor
            factor_count += 1

        return scales
