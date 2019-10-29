import logging
from typing import Union, List

import numpy as np
import tensorflow as tf
from skimage import transform

from src import pyutils
from src.dto import BoundingBox
from src.dto.cropped_face import CroppedFace
from src.face_recognition.embedding_classifier.libraries import facenet
from src.face_recognition.face_cropper.constants import FACE_MIN_SIZE, THRESHOLD, SCALE_FACTOR, FaceLimitConstant, \
    MARGIN, IMAGE_SIZE
from src.face_recognition.face_cropper.exceptions import IncorrectImageDimensionsError, NoFaceFoundError
from src.face_recognition.face_cropper.libraries.align import detect_face

pnet, rnet, onet = None, None, None


@pyutils.run_once
def _init_once():
    with tf.Graph().as_default():
        global pnet, rnet, onet
        sess = tf.Session()
        pnet, rnet, onet = detect_face.create_mtcnn(sess, None)


def crop_face(img) -> CroppedFace:
    cropped_faces = crop_faces(img, face_lim=1)
    return cropped_faces[0]


def _preprocess_img(img):
    if img.ndim < 2:
        raise IncorrectImageDimensionsError("Unable to align image, it has only one dimension")

    if img.ndim == 2:
        img = facenet.to_rgb(img)
    return img[:, :, 0:3]


def _get_face_bounding_boxes(img):
    bounding_boxes, _ = detect_face.detect_face(img, FACE_MIN_SIZE, pnet, rnet, onet, THRESHOLD, SCALE_FACTOR)
    return bounding_boxes


@pyutils.run_first(_init_once)
def crop_faces(img, face_lim: Union[int, FaceLimitConstant] = FaceLimitConstant.NO_LIMIT) -> List[CroppedFace]:
    img = _preprocess_img(img)
    bounding_boxes = _get_face_bounding_boxes(img)
    face_count = bounding_boxes.shape[0]
    if face_count < 1:
        raise NoFaceFoundError("No face is found in the given image")
    face_lim = face_lim or face_count

    det = bounding_boxes[:, 0:4]
    img_size = np.asarray(img.shape)[0:2]
    detected = []
    if face_count > 1:
        img_center = img_size / 2
        for start in range(face_lim):
            bounding_box_size = (det[start:, 2] - det[start:, 0]) * (det[start:, 3] - det[start:, 1])

            offsets = np.vstack(
                [(det[start, 0] + det[start, 2]) / 2 - img_center[1],
                 (det[start, 1] + det[start, 3]) / 2 - img_center[0]])
            offset_dist_squared = np.sum(np.power(offsets, 2.0), 0)
            index = np.argmax(bounding_box_size - offset_dist_squared * 2.0)  # some extra weight on the centering
            detected.append(det[index, :])
    else:
        detected.append(det)

    faces = []
    for elem in detected:
        logging.debug(f'the box around this face has dimensions of {elem[0:4]}')
        det = np.squeeze(elem)
        bb = np.zeros(4, dtype=np.int32)
        bb[0] = np.maximum(det[0] - MARGIN / 2, 0)
        bb[1] = np.maximum(det[1] - MARGIN / 2, 0)
        bb[2] = np.minimum(det[2] + MARGIN / 2, img_size[1])
        bb[3] = np.minimum(det[3] + MARGIN / 2, img_size[0])
        cropped = img[bb[1]:bb[3], bb[0]:bb[2], :]
        resized = transform.resize(cropped, (IMAGE_SIZE, IMAGE_SIZE))
        face = CroppedFace(box=BoundingBox(xmin=bb[0], ymin=bb[1], xmax=bb[2], ymax=bb[3]), img=resized)
        faces.append(face)

    return faces