import math
from collections import namedtuple

import numpy as np
import tensorflow as tf

from src import pyutils
from src.scan_faces._calc_embedding._face_crop import crop_image
from src.scan_faces.dto.bounding_box import BoundingBox
from src.scan_faces.dto.embedding import Embedding
from src.storage.constants import EMBEDDING_CALCULATOR_MODEL_FILENAME
from src.storage.storage import get_storage

BATCH_SIZE = 25
CALCULATOR_VERSION = EMBEDDING_CALCULATOR_MODEL_FILENAME

Calculator = namedtuple('Calculator', 'graph sess')


@pyutils.run_once
def _calculator() -> Calculator:
    with tf.Graph().as_default() as graph:
        graph_def = tf.GraphDef()
        graph_def.ParseFromString(get_storage().get_file(CALCULATOR_VERSION))
        tf.import_graph_def(graph_def, name='')
        return Calculator(graph=graph, sess=tf.Session(graph=graph))


def _calculate_embeddings(cropped_images):
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

    # Return DTO
    return [Embedding(array=emb, calculator_version=CALCULATOR_VERSION) for emb in emb_array]


def calculate_embedding(image: np.ndarray, box: BoundingBox) -> Embedding:
    cropped_image = crop_image(image, box)
    return _calculate_embeddings([cropped_image])[0]
