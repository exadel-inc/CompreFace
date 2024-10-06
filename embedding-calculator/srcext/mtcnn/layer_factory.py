#!/usr/bin/python3
# -*- coding: utf-8 -*-

#MIT License
#
#Copyright (c) 2018 Iván de Paz Centeno
#
#Permission is hereby granted, free of charge, to any person obtaining a copy
#of this software and associated documentation files (the "Software"), to deal
#in the Software without restriction, including without limitation the rights
#to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#copies of the Software, and to permit persons to whom the Software is
#furnished to do so, subject to the following conditions:
#
#The above copyright notice and this permission notice shall be included in all
#copies or substantial portions of the Software.
#
#THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#SOFTWARE.

import tensorflow as tf
from distutils.version import LooseVersion

__author__ = "Iván de Paz Centeno"


class LayerFactory(object):
    """
    Allows to create stack layers for a given network.
    """

    AVAILABLE_PADDINGS = ('SAME', 'VALID')

    def __init__(self, network):
        self.__network = network

    @staticmethod
    def __validate_padding(padding):
        if padding not in LayerFactory.AVAILABLE_PADDINGS:
            raise Exception("Padding {} not valid".format(padding))

    @staticmethod
    def __validate_grouping(channels_input: int, channels_output: int, group: int):
        if channels_input % group != 0:
            raise Exception("The number of channels in the input does not match the group")

        if channels_output % group != 0:
            raise Exception("The number of channels in the output does not match the group")

    @staticmethod
    def vectorize_input(input_layer):
        input_shape = input_layer.get_shape()

        if input_shape.ndims == 4:
            # Spatial input, must be vectorized.
            dim = 1
            for x in input_shape[1:].as_list():
                dim *= int(x)

            #dim = operator.mul(*(input_shape[1:].as_list()))
            vectorized_input = tf.reshape(input_layer, [-1, dim])
        else:
            vectorized_input, dim = (input_layer, input_shape[-1])

        return vectorized_input, dim

    def __make_var(self, name: str, shape: list):
        """
        Creates a tensorflow variable with the given name and shape.
        :param name: name to set for the variable.
        :param shape: list defining the shape of the variable.
        :return: created TF variable.
        """
        return tf.compat.v1.get_variable(name, shape, trainable=self.__network.is_trainable(),
                                         use_resource=False)

    def new_feed(self, name: str, layer_shape: tuple):
        """
        Creates a feed layer. This is usually the first layer in the network.
        :param name: name of the layer
        :return:
        """

        feed_data = tf.compat.v1.placeholder(tf.float32, layer_shape, 'input')
        self.__network.add_layer(name, layer_output=feed_data)

    def new_conv(self, name: str, kernel_size: tuple, channels_output: int,
                 stride_size: tuple, padding: str='SAME',
                 group: int=1, biased: bool=True, relu: bool=True, input_layer_name: str=None):
        """
        Creates a convolution layer for the network.
        :param name: name for the layer
        :param kernel_size: tuple containing the size of the kernel (Width, Height)
        :param channels_output: ¿? Perhaps number of channels in the output? it is used as the bias size.
        :param stride_size: tuple containing the size of the stride (Width, Height)
        :param padding: Type of padding. Available values are: ('SAME', 'VALID')
        :param group: groups for the kernel operation. More info required.
        :param biased: boolean flag to set if biased or not.
        :param relu: boolean flag to set if ReLu should be applied at the end of the layer or not.
        :param input_layer_name: name of the input layer for this layer. If None, it will take the last added layer of
        the network.
        """

        # Verify that the padding is acceptable
        self.__validate_padding(padding)

        input_layer = self.__network.get_layer(input_layer_name)

        # Get the number of channels in the input
        channels_input = int(input_layer.get_shape()[-1])

        # Verify that the grouping parameter is valid
        self.__validate_grouping(channels_input, channels_output, group)

        # Convolution for a given input and kernel
        convolve = lambda input_val, kernel: tf.nn.conv2d(input=input_val,
                filters=kernel, 
                strides=[1, stride_size[1], stride_size[0], 1],
                padding=padding)

        with tf.compat.v1.variable_scope(name) as scope:
            kernel = self.__make_var('weights', shape=[kernel_size[1], kernel_size[0], channels_input // group, channels_output])

            output = convolve(input_layer, kernel)

            # Add the biases, if required
            if biased:
                biases = self.__make_var('biases', [channels_output])
                output = tf.nn.bias_add(output, biases)

            # Apply ReLU non-linearity, if required
            if relu:
                output = tf.nn.relu(output, name=scope.name)


        self.__network.add_layer(name, layer_output=output)

    def new_prelu(self, name: str, input_layer_name: str=None):
        """
        Creates a new prelu layer with the given name and input.
        :param name: name for this layer.
        :param input_layer_name: name of the layer that serves as input for this one.
        """
        input_layer = self.__network.get_layer(input_layer_name)

        with tf.compat.v1.variable_scope(name):
            channels_input = int(input_layer.get_shape()[-1])
            alpha = self.__make_var('alpha', shape=[channels_input])
            output = tf.nn.relu(input_layer) + tf.multiply(alpha, -tf.nn.relu(-input_layer))

        self.__network.add_layer(name, layer_output=output)

    def new_max_pool(self, name:str, kernel_size: tuple, stride_size: tuple, padding='SAME',
                     input_layer_name: str=None):
        """
        Creates a new max pooling layer.
        :param name: name for the layer.
        :param kernel_size: tuple containing the size of the kernel (Width, Height)
        :param stride_size: tuple containing the size of the stride (Width, Height)
        :param padding: Type of padding. Available values are: ('SAME', 'VALID')
        :param input_layer_name: name of the input layer for this layer. If None, it will take the last added layer of
        the network.
        """

        self.__validate_padding(padding)

        input_layer = self.__network.get_layer(input_layer_name)

        output = tf.nn.max_pool2d(input=input_layer,
                                ksize=[1, kernel_size[1], kernel_size[0], 1],
                                strides=[1, stride_size[1], stride_size[0], 1],
                                padding=padding,
                                name=name)

        self.__network.add_layer(name, layer_output=output)

    def new_fully_connected(self, name: str, output_count: int, relu=True, input_layer_name: str=None):
        """
        Creates a new fully connected layer.

        :param name: name for the layer.
        :param output_count: number of outputs of the fully connected layer.
        :param relu: boolean flag to set if ReLu should be applied at the end of this layer.
        :param input_layer_name: name of the input layer for this layer. If None, it will take the last added layer of
        the network.
        """

        with tf.compat.v1.variable_scope(name):
            input_layer = self.__network.get_layer(input_layer_name)
            vectorized_input, dimension = self.vectorize_input(input_layer)

            weights = self.__make_var('weights', shape=[dimension, output_count])
            biases = self.__make_var('biases', shape=[output_count])
            operation = tf.compat.v1.nn.relu_layer if relu else tf.compat.v1.nn.xw_plus_b

            fc = operation(vectorized_input, weights, biases, name=name)

        self.__network.add_layer(name, layer_output=fc)

    def new_softmax(self, name, axis, input_layer_name: str=None):
        """
        Creates a new softmax layer
        :param name: name to set for the layer
        :param axis:
        :param input_layer_name: name of the input layer for this layer. If None, it will take the last added layer of
        the network.
        """
        input_layer = self.__network.get_layer(input_layer_name)

        if LooseVersion(tf.__version__) < LooseVersion("1.5.0"):
            max_axis = tf.reduce_max(input_tensor=input_layer, axis=axis, keepdims=True)
            target_exp = tf.exp(input_layer - max_axis)
            normalize = tf.reduce_sum(input_tensor=target_exp, axis=axis, keepdims=True)
        else:
            max_axis = tf.reduce_max(input_tensor=input_layer, axis=axis, keepdims=True)
            target_exp = tf.exp(input_layer - max_axis)
            normalize = tf.reduce_sum(input_tensor=target_exp, axis=axis, keepdims=True)

        softmax = tf.math.divide(target_exp, normalize, name)

        self.__network.add_layer(name, layer_output=softmax)

