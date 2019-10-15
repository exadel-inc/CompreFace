import logging
from typing import Union

import numpy as np
import tensorflow as tf
from skimage import transform

from src.core.constants import FaceLimit
from src.core.exceptions import NoFaceIsFoundError, OneDimensionalImageIsGivenError
from src.core.libraries import facenet
from src.core.libraries.align import detect_face

minsize = 20  # minimum size of face
threshold = [0.6, 0.7, 0.7]  # three steps's threshold
factor = 0.709  # scale factor
margin = 32
image_size = 160


def init():
    with tf.Graph().as_default():
        global pnet, rnet, onet
        sess = tf.Session()
        pnet, rnet, onet = detect_face.create_mtcnn(sess, None)


def crop_face(img):
    return crop_faces(img, 1)[0]


def crop_faces(img, face_lim: Union[int, FaceLimit] = FaceLimit.NO_LIMIT):
    if img.ndim < 2:
        raise OneDimensionalImageIsGivenError("Unable to align image, it has only one dimension")
    if img.ndim == 2:
        img = facenet.to_rgb(img)
    img = img[:, :, 0:3]
    bounding_boxes, _ = detect_face.detect_face(img, minsize, pnet, rnet, onet, threshold,
                                                factor)
    nrof_faces = bounding_boxes.shape[0]
    if nrof_faces < 1:
        raise NoFaceIsFoundError("Haven't found face")
    det = bounding_boxes[:, 0:4]
    img_size = np.asarray(img.shape)[0:2]
    detected = []
    if face_lim != FaceLimit.NO_LIMIT:
        range_lim = face_lim
    else:
        range_lim = nrof_faces
    if nrof_faces > 1:
        logging.debug(nrof_faces)
        img_center = img_size / 2
        for start in range(range_lim):
            bounding_box_size = (det[start:, 2] - det[start:, 0]) * (det[start:, 3] - det[start:, 1])

            offsets = np.vstack(
                [(det[start, 0] + det[start, 2]) / 2 - img_center[1],
                 (det[start, 1] + det[start, 3]) / 2 - img_center[0]])
            offset_dist_squared = np.sum(np.power(offsets, 2.0), 0)
            index = np.argmax(bounding_box_size - offset_dist_squared * 2.0)  # some extra weight on the centering
            detected.append(det[index, :])
    else:
        detected.append(det)

    transformedPics = []
    for elem in detected:
        logging.debug('the box around this face has dimensions of', elem[0:4])
        det = np.squeeze(elem)
        bb = np.zeros(4, dtype=np.int32)
        bb[0] = np.maximum(det[0] - margin / 2, 0)
        bb[1] = np.maximum(det[1] - margin / 2, 0)
        bb[2] = np.minimum(det[2] + margin / 2, img_size[1])
        bb[3] = np.minimum(det[3] + margin / 2, img_size[0])
        cropped = img[bb[1]:bb[3], bb[0]:bb[2], :]
        transformedPics.append(transform.resize(cropped, (image_size, image_size)))
        transformedPics.append(elem[0:4])

    return transformedPics
