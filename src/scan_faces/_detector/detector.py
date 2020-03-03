from collections import namedtuple
from typing import List

import numpy as np
import tensorflow as tf
from numpy.core._multiarray_umath import ndarray

from src import _pyutils
from src.scan_faces._detector._lib import facenet
from src.scan_faces._detector._lib.align import detect_face
from src.scan_faces._detector.constants import SCALE_FACTOR, DEFAULT_THRESHOLD_A, DEFAULT_THRESHOLD_B, \
    FACE_MIN_SIZE, BOX_MARGIN, FaceLimitConstant, DEFAULT_THRESHOLD_C
from src.scan_faces._detector.exceptions import NoFaceFoundError, IncorrectImageDimensionsError
from src.scan_faces.dto.bounding_box import BoundingBox
from src.scan_faces.dto.face import DetectedFace

FaceDetectionNets = namedtuple('FaceDetectionNets', 'pnet rnet onet')


@_pyutils.run_once
def _face_detection_nets():
    with tf.Graph().as_default():
        sess = tf.Session()
        return FaceDetectionNets(*detect_face.create_mtcnn(sess, None))


def _preprocess_img(img: ndarray):
    if img.ndim < 2:
        raise IncorrectImageDimensionsError("Unable to align image, it has only one dimension")
    img = facenet.to_rgb(img) if img.ndim == 2 else img
    img = img[:, :, 0:3]
    return img


def detect_faces(img, face_limit=FaceLimitConstant.NO_LIMIT, detection_threshold_c=DEFAULT_THRESHOLD_C) -> List[
    DetectedFace]:
    img = _preprocess_img(img)
    fdn = _face_detection_nets()
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
