import math

import numpy as np
import tensorflow as tf

from src.database import get_storage

BATCH_SIZE = 25


def init():
    global graph
    with tf.Graph().as_default() as graph:
        graph_def = tf.GraphDef()
        graph_def.ParseFromString(get_storage().get_model())
        tf.import_graph_def(graph_def, name='')
        global sess
        sess = tf.Session(graph=graph)


def calc_embedding(image):
    return _calc_embeddings(np.array([image]))[0]


def _calc_embeddings(images):
    # Get inppredictut and output tensors
    images_placeholder = graph.get_tensor_by_name("input:0")
    embeddings = graph.get_tensor_by_name("embeddings:0")
    phase_train_placeholder = graph.get_tensor_by_name("phase_train:0")
    embedding_size = embeddings.get_shape()[1]
    # Run forward pass to calculate embeddings
    nrof_images = len(images)
    nrof_batches_per_epoch = int(math.ceil(1.0 * nrof_images / BATCH_SIZE))
    emb_array = np.zeros((nrof_images, embedding_size))
    for i in range(nrof_batches_per_epoch):
        start_index = i * BATCH_SIZE
        end_index = min((i + 1) * BATCH_SIZE, nrof_images)
        feed_dict = {images_placeholder: images, phase_train_placeholder: False}
        emb_array[start_index:end_index, :] = sess.run(embeddings, feed_dict=feed_dict)
    return emb_array
