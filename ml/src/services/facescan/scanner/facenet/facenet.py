import math
from collections import namedtuple
from typing import List

import numpy as np
import tensorflow as tf
from srcext.facenet.align import detect_face
from tensorflow.python.platform import gfile

from src.exceptions import NoFaceFoundError
from src.services.dto.bounding_box import BoundingBox
from src.services.dto.scanned_face import ScannedFace
from src.services.facescan.imgscaler.imgscaler import ImgScaler
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.facescan.scanner.constants import NO_LIMIT
from src.services.imgtools.types import Array3D
from src.services.imgtools.proc_img import crop_img, squish_img
from src.services.utils.pyutils import get_dir

CURRENT_DIR = get_dir(__file__)
_EmbeddingCalculator = namedtuple('Calculator', 'graph sess')
_FaceDetectionNets = namedtuple('FaceDetectionNets', 'pnet rnet onet')


class Facenet2018(FaceScanner):
    ID = 'Facenet2018'
    BATCH_SIZE = 25
    FACE_MIN_SIZE = 20
    SCALE_FACTOR = 0.709
    BOX_MARGIN = 32
    DEFAULT_THRESHOLD_A = 0.9436513301
    DEFAULT_THRESHOLD_B = 0.7059968943
    DEFAULT_THRESHOLD_C = 0.5506904359
    IMAGE_SIZE = 160
    IMG_LENGTH_LIMIT = 1000
    EMBEDDING_MODEL_PATH = CURRENT_DIR / 'model' / 'embedding_calc_model_20180402.pb'

    def __init__(self):
        super().__init__()
        self._embedding_calculator = self._get_embedding_calculator()
        self._face_detection_nets = self._get_face_detection_nets()

    def _get_embedding_calculator(self):
        with tf.Graph().as_default() as graph:
            graph_def = tf.GraphDef()
            with gfile.FastGFile(str(self.EMBEDDING_MODEL_PATH), 'rb') as f:
                model = f.read()
            graph_def.ParseFromString(model)
            tf.import_graph_def(graph_def, name='')
            return _EmbeddingCalculator(graph=graph, sess=tf.Session(graph=graph))

    @staticmethod
    def _get_face_detection_nets():
        with tf.Graph().as_default():
            sess = tf.Session()
            return _FaceDetectionNets(*detect_face.create_mtcnn(sess, None))

    def _find_face_bounding_boxes(self, img, face_limit, facenet_detection_threshold_c=None) \
            -> List[BoundingBox]:
        facenet_detection_threshold_c = facenet_detection_threshold_c or self.DEFAULT_THRESHOLD_C
        fdn = self._face_detection_nets
        detect_face_result = detect_face.detect_face(img, self.FACE_MIN_SIZE, fdn.pnet, fdn.rnet, fdn.onet,
                                                     [self.DEFAULT_THRESHOLD_A, self.DEFAULT_THRESHOLD_B,
                                                      facenet_detection_threshold_c], self.SCALE_FACTOR)
        img_size = np.asarray(img.shape)[0:2]
        bounding_boxes = []
        for result_item in detect_face_result[0]:
            result_item = np.squeeze(result_item)
            margin = self.BOX_MARGIN / 2
            bounding_box = BoundingBox(
                x_min=int(np.maximum(result_item[0] - margin, 0)),
                y_min=int(np.maximum(result_item[1] - margin, 0)),
                x_max=int(np.minimum(result_item[2] + margin, img_size[1])),
                y_max=int(np.minimum(result_item[3] + margin, img_size[0])),
                probability=result_item[4]
            )
            bounding_boxes.append(bounding_box)

        if len(bounding_boxes) == 0:
            raise NoFaceFoundError
        if face_limit:
            return bounding_boxes[:face_limit]
        return bounding_boxes

    def _calculate_embeddings(self, cropped_images):
        """Run forward pass to calculate embeddings"""
        graph_images_placeholder = self._embedding_calculator.graph.get_tensor_by_name("input:0")
        graph_embeddings = self._embedding_calculator.graph.get_tensor_by_name("embeddings:0")
        graph_phase_train_placeholder = self._embedding_calculator.graph.get_tensor_by_name("phase_train:0")
        embedding_size = graph_embeddings.get_shape()[1]
        image_count = len(cropped_images)
        batches_per_epoch = int(math.ceil(1.0 * image_count / self.BATCH_SIZE))
        embeddings = np.zeros((image_count, embedding_size))
        for i in range(batches_per_epoch):
            start_index = i * self.BATCH_SIZE
            end_index = min((i + 1) * self.BATCH_SIZE, image_count)
            feed_dict = {graph_images_placeholder: cropped_images, graph_phase_train_placeholder: False}
            embeddings[start_index:end_index, :] = self._embedding_calculator.sess.run(graph_embeddings, feed_dict=feed_dict)
        return embeddings

    def scan(self, img: Array3D, face_limit: int = NO_LIMIT, detection_threshold: float = None) -> List[ScannedFace]:
        detection_threshold_c = self.DEFAULT_THRESHOLD_C if detection_threshold is None else detection_threshold
        assert 0 <= detection_threshold_c <= 1
        scanned_faces = []
        scaler = ImgScaler(self.IMG_LENGTH_LIMIT)
        downscaled_img = scaler.downscale_img(img)
        for downscaled_box in self._find_face_bounding_boxes(downscaled_img, face_limit, detection_threshold_c):
            box = scaler.upscale_box(downscaled_box)
            cropped_img = crop_img(img, box)
            squished_img = squish_img(cropped_img, (self.IMAGE_SIZE, self.IMAGE_SIZE))
            embedding = self._calculate_embeddings([squished_img])[0]
            scanned_face = ScannedFace(embedding=embedding, box=box, img=img, face_img=cropped_img)
            scanned_faces.append(scanned_face)
        return scanned_faces
