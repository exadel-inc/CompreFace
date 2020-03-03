import math
import os
from collections import namedtuple
from pathlib import Path

import numpy as np
import tensorflow as tf
from tensorflow.python.platform import gfile

from src import _pyutils
from src.scan_faces._embedder._face_crop import crop_image
from src.scan_faces._embedder.constants import EMBEDDING_CALCULATOR_MODEL_FILENAME
from src.scan_faces.dto.bounding_box import BoundingBox
from src.scan_faces.dto.embedding import Embedding

BATCH_SIZE = 25


Calculator = namedtuple('Calculator', 'graph sess')

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
MODEL_PATH = CURRENT_DIR / '_model' / EMBEDDING_CALCULATOR_MODEL_FILENAME


def _get_model():
    with gfile.FastGFile(str(MODEL_PATH), 'rb') as f:
        return f.read()


@_pyutils.run_once
def _calculator() -> Calculator:
    with tf.Graph().as_default() as graph:
        graph_def = tf.GraphDef()
        graph_def.ParseFromString(_get_model())
        tf.import_graph_def(graph_def, name='')
        return Calculator(graph=graph, sess=tf.Session(graph=graph))


def calculate_embeddings(cropped_images):
    calculator = _calculator()

    # Get tensors and constants
    images_placeholder = calculator.graph.get_tensor_by_name("input:0")
    embeddings = calculator.graph.get_tensor_by_name("embeddings:0")
    phase_train_placeholder = calculator.graph.get_tensor_by_name("phase_train:0")
    embedding_size = embeddings.get_shape()[1]

    # Run forward pass to calculate embeddings
    image_count = len(cropped_images)
    batches_per_epoch = int(math.ceil(1.0 * image_count / BATCH_SIZE))
    emb_array = np.zeros((image_count, embedding_size))
    for i in range(batches_per_epoch):
        start_index = i * BATCH_SIZE
        end_index = min((i + 1) * BATCH_SIZE, image_count)
        feed_dict = {images_placeholder: cropped_images, phase_train_placeholder: False}
        emb_array[start_index:end_index, :] = calculator.sess.run(embeddings, feed_dict=feed_dict)

    # Return embeddings
    return [emb.tolist() for emb in emb_array]


def calculate_embedding(image: np.ndarray, box: BoundingBox) -> Embedding:
    cropped_image = crop_image(image, box)
    return calculate_embeddings([cropped_image])[0]
