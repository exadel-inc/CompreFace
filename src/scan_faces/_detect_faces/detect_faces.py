from collections import namedtuple
from typing import List

import numpy as np
import tensorflow as tf

from src import pyutils
from src.scan_faces._detect_faces._lib.align import detect_face
from src.scan_faces._detect_faces.constants import SCALE_FACTOR, DEFAULT_THRESHOLD_A, DEFAULT_THRESHOLD_B, \
    FACE_MIN_SIZE, BOX_MARGIN
from src.scan_faces._detect_faces.exceptions import NoFaceFoundError, IncorrectImageDimensionsError
from src.scan_faces.dto.bounding_box import BoundingBox
from src.scan_faces.dto.cropped_face import DetectedFace

FaceDetectionNets = namedtuple('FaceDetectionNets', 'pnet rnet onet')


@pyutils.run_once
def _face_detection_nets():
    with tf.Graph().as_default():
        sess = tf.Session()
        return FaceDetectionNets(*detect_face.create_mtcnn(sess, None))


def detect_faces(img, face_limit, detection_threshold_c) -> List[DetectedFace]:
    fdn = _face_detection_nets()
    if len(img.shape) < 2:
        raise IncorrectImageDimensionsError
    detect_face_result = detect_face.detect_face(img, FACE_MIN_SIZE, fdn.pnet, fdn.rnet, fdn.onet,
                                                 [DEFAULT_THRESHOLD_A, DEFAULT_THRESHOLD_B, detection_threshold_c],
                                                 SCALE_FACTOR)
    img_size = np.asarray(img.shape)[0:2]
    detected_faces = []
    for result_item in detect_face_result[0]:
        result_item = np.squeeze(result_item)
        margin = BOX_MARGIN / 2
        bounding_box = BoundingBox(
            x_min=int(np.maximum(result_item[0] - margin, 0)),
            y_min=int(np.maximum(result_item[1] - margin, 0)),
            x_max=int(np.minimum(result_item[2] + margin, img_size[1])),
            y_max=int(np.minimum(result_item[3] + margin, img_size[0])),
            probability=result_item[4]
        )
        detected_faces.append(DetectedFace(box=bounding_box))

    if len(detected_faces) < 1:
        raise NoFaceFoundError("No face is found in the given image")
    if face_limit:
        return detected_faces[:face_limit]
    return detected_faces
