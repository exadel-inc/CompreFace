import logging
from typing import List

import numpy as np
import tensorflow as tf
from skimage import transform

from src import pyutils
from src.face_recognition.dto.bounding_box import BoundingBox
from src.face_recognition.dto.cropped_face import CroppedFace
from src.face_recognition.embedding_classifier.libraries import facenet
from src.face_recognition.face_cropper.constants import FACE_MIN_SIZE, THRESHOLD, SCALE_FACTOR, FaceLimitConstant, \
    MARGIN, IMAGE_SIZE, FaceLimit
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



def _get_bounding_boxes(img, face_lim):
    detect_face_result = detect_face.detect_face(img, FACE_MIN_SIZE, pnet, rnet, onet, THRESHOLD, SCALE_FACTOR)
    bounding_boxes = list(detect_face_result[0][:, 0:4])
    if len(bounding_boxes) < 1:
        raise NoFaceFoundError("No face is found in the given image")
    if face_lim and face_lim <= len(bounding_boxes):
        return bounding_boxes[:face_lim]
    return list(bounding_boxes)



def _bounding_box_2_cropped_face(bounding_box, img, img_size) -> CroppedFace:
    logging.debug(f"the box around this face has dimensions of {bounding_box[0:4]}")
    bounding_box = np.squeeze(bounding_box)
    xmin = int(np.maximum(bounding_box[0] - MARGIN / 2, 0))
    ymin = int(np.maximum(bounding_box[1] - MARGIN / 2, 0))
    xmax = int(np.minimum(bounding_box[2] + MARGIN / 2, img_size[1]))
    ymax = int(np.minimum(bounding_box[3] + MARGIN / 2, img_size[0]))
    cropped_img = img[ymin:ymax, xmin:xmax, :]
    resized_img = transform.resize(cropped_img, (IMAGE_SIZE, IMAGE_SIZE))
    return CroppedFace(box=BoundingBox(xmin=xmin, ymin=ymin, xmax=xmax, ymax=ymax), img=resized_img)


def _preprocess_img(img):
    if img.ndim < 2:
        raise IncorrectImageDimensionsError("Unable to align image, it has only one dimension")
    img = facenet.to_rgb(img) if img.ndim == 2 else img
    img = img[:, :, 0:3]
    img_size = np.asarray(img.shape)[0:2]
    return img, img_size


@pyutils.run_first(_init_once)
def crop_faces(img, face_lim: FaceLimit = FaceLimitConstant.NO_LIMIT) -> List[CroppedFace]:
    img, img_size = _preprocess_img(img)
    bounding_boxes = _get_bounding_boxes(img, face_lim)
    cropped_faces = [_bounding_box_2_cropped_face(bounding_box, img, img_size) for bounding_box in bounding_boxes]
    return cropped_faces
