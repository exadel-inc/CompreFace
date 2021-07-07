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
import tensorflow.compat.v1 as tf1
import re
import tf_slim
from tf_slim.nets.inception_v3 import inception_v3_base


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
    weights_regularizer = tf_slim.l2_regularizer(weight_decay)

    args_for_scope = (
        dict(list_ops_or_scope=[tf_slim.layers.conv2d, tf_slim.layers.fully_connected],
             weights_regularizer=weights_regularizer, trainable=True),
        dict(list_ops_or_scope=[tf_slim.layers.conv2d],
             weights_initializer=tf1.truncated_normal_initializer(stddev=stddev),
             activation_fn=tf1.nn.relu,
             normalizer_fn=tf_slim.layers.batch_norm,
             normalizer_params=batch_norm_params),
    )

    with tf1.variable_scope("InceptionV3", "InceptionV3", [images]) as scope, \
            tf_slim.arg_scope(**args_for_scope[0]), \
            tf_slim.arg_scope(**args_for_scope[1]):
        net, end_points = inception_v3_base(images, scope=scope)
        with tf1.variable_scope("logits"):
            shape = net.get_shape()
            net = tf_slim.layers.avg_pool2d(net, shape[1:3], padding="VALID",
                                    scope="pool")
            net = tf1.nn.dropout(net, 1, name='droplast')
            net = tf_slim.layers.flatten(net, scope="flatten")

    with tf1.variable_scope('output') as scope:
        weights = tf1.Variable(
            tf1.truncated_normal([2048, nlabels], mean=0.0, stddev=0.01),
            name='weights')
        biases = tf1.Variable(
            tf1.constant(0.0, shape=[nlabels], dtype=tf1.float32), name='biases')
        output = tf1.add(tf1.matmul(net, weights), biases, name=scope.name)

        tensor_name = re.sub('tower_[0-9]*/', '', output.op.name)
        tf1.summary.histogram(tensor_name + '/activations', output)
        tf1.summary.scalar(tensor_name + '/sparsity', tf1.nn.zero_fraction(output))
    return output

