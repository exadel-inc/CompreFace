#  Copyright (c) 2020 the original author or authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

import numpy as np
import os
import tensorflow as tf
import re
from tensorflow.contrib import layers, slim
from tensorflow.contrib.slim.python.slim.nets.inception_v3 import inception_v3_base


def prewhiten(img):
    """ Normalize image."""
    mean = np.mean(img)
    std = np.std(img)
    std_adj = np.maximum(std, 1.0 / np.sqrt(img.size))
    y = np.multiply(np.subtract(img, mean), 1 / std_adj)
    return y


def inception_v3(nlabels, images):
    batch_norm_params = {
        "is_training": False, "trainable": True, "decay": 0.9997,
        "epsilon": 0.001,
        "variables_collections": {
            "beta": None,
            "gamma": None,
            "moving_mean": ["moving_vars"],
            "moving_variance": ["moving_vars"],
        }
    }
    weight_decay = 0.00004
    stddev = 0.1
    weights_regularizer = layers.l2_regularizer(weight_decay)

    args_for_scope = (
        dict(list_ops_or_scope=[slim.conv2d, slim.fully_connected],
             weights_regularizer=weights_regularizer, trainable=True),
        dict(list_ops_or_scope=[slim.conv2d],
             weights_initializer=tf.truncated_normal_initializer(stddev=stddev),
             activation_fn=tf.nn.relu,
             normalizer_fn=layers.batch_norm,
             normalizer_params=batch_norm_params),
    )

    with tf.variable_scope("InceptionV3", "InceptionV3", [images]) as scope, \
            slim.arg_scope(**args_for_scope[0]), \
            slim.arg_scope(**args_for_scope[1]):
        net, end_points = inception_v3_base(images, scope=scope)
        with tf.variable_scope("logits"):
            shape = net.get_shape()
            net = layers.avg_pool2d(net, shape[1:3], padding="VALID",
                                    scope="pool")
            net = tf.nn.dropout(net, 1, name='droplast')
            net = layers.flatten(net, scope="flatten")

    with tf.variable_scope('output') as scope:
        weights = tf.Variable(
            tf.truncated_normal([2048, nlabels], mean=0.0, stddev=0.01),
            name='weights')
        biases = tf.Variable(
            tf.constant(0.0, shape=[nlabels], dtype=tf.float32), name='biases')
        output = tf.add(tf.matmul(net, weights), biases, name=scope.name)

        tensor_name = re.sub('tower_[0-9]*/', '', output.op.name)
        tf.summary.histogram(tensor_name + '/activations', output)
        tf.summary.scalar(tensor_name + '/sparsity', tf.nn.zero_fraction(output))
    return output
