import os
from pathlib import Path

import imageio
from src import pyutils

import tensorflow as tf
from operator import itemgetter

from src.face_recognition.embedding_classifier.libraries import facenet
from src.face_recognition.face_cropper.constants import FACE_MIN_SIZE, SCALE_FACTOR, FaceLimitConstant,FaceLimit, MARGIN
from src.face_recognition.face_cropper.cropper import _preprocess_img
from src.face_recognition.face_cropper.libraries.align import detect_face
from src.face_recognition.face_cropper.exceptions import IncorrectImageDimensionsError, NoFaceFoundError

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
pnet, rnet, onet = None, None, None

@pyutils.run_once
def _init_once():
    with tf.Graph().as_default():
        global pnet, rnet, onet
        sess = tf.Session()
        pnet, rnet, onet = detect_face.create_mtcnn(sess, None)


def _preprocess_img(img):
    if img.ndim < 2:
        raise IncorrectImageDimensionsError("Unable to align image, it has only one dimension")
    img = facenet.to_rgb(img) if img.ndim == 2 else img
    img = img[:, :, 0:3]
    return img

def _get_bounding_boxes_for_threshold(img, threshold, face_lim: FaceLimit = FaceLimitConstant.NO_LIMIT):

    detect_face_result = detect_face.detect_face(img, FACE_MIN_SIZE, pnet, rnet, onet, threshold, SCALE_FACTOR)
    bounding_boxes = list(detect_face_result[0][:, 0:4])
    if len(bounding_boxes) < 1:
        raise NoFaceFoundError("No face is found in the given image")
    if face_lim:
        return bounding_boxes[:face_lim]
    return bounding_boxes


def _calculate_threshold(list_of_thresholds):

    maxVal = list_of_thresholds[0][1]
    sum_x = list_of_thresholds[0][0][0]
    count_x = 1
    sum_y = list_of_thresholds[0][0][1]
    count_y = 1
    sum_z = list_of_thresholds[0][0][2]
    count_z = 1
    for pair in range(1, len(list_of_thresholds)):
        if list_of_thresholds[pair][0][1] < maxVal:
            return [int(sum_x / count_x) * 0.1, int(sum_y / count_y) * 0.1, int(sum_z / count_z) * 0.1]
        else:
            sum_x += list_of_thresholds[pair][0][0]
            count_x += 1
            sum_y += list_of_thresholds[pair][0][1]
            count_y += 1
            sum_z += list_of_thresholds[pair][0][2]
            count_z += 1

if __name__ == "__main__" :
    _init_once()

    dict_faces_and_noses = {CURRENT_DIR / 'files' / 'four-faces.png': [(996,469), (650, 457), (788,450), (1157, 455)],
                            CURRENT_DIR / 'files' / 'four-faces.jpg': [(212,209),(319,238),(443,234),(629,248)],
                            CURRENT_DIR / 'files' / 'four-plus-one-faces.png': [(660,630), (808, 618), (898,665), (972, 513), (1153, 576)],
                            CURRENT_DIR / 'files' / 'four-plus-one-faces.jpg': [(281,268),(344,261),(383,283),(413,221),(494,248)],
                            CURRENT_DIR / 'files' / 'five-people.png': [(225, 666), (484, 625), (956, 419), (1417, 638), (1617, 716)],
                            #CURRENT_DIR / 'files' / 'five-people.jpg': [(95, 283), (207, 262), (407, 175), (605, 270), (691, 305)],
                            #CURRENT_DIR / 'files' / 'six-faces.png': [(384, 537), (632, 631), (823,666), (1060, 636), (1306, 623), (1494, 590)],
                            #CURRENT_DIR / 'files' / 'six-faces.jpg': [(164, 229), (269, 269), (352,282), (453,269), (557,263), (635,250)],
                            #CURRENT_DIR / 'files' / 'eight-faces.png': [(461,651),(612,686),(619,400),(836,663),(1034,499),(1064,676),(1217,386),(1613,479)],
                            #CURRENT_DIR / 'files' / 'eight-faces.jpg': [(194,277),(262,169),(260,292),(357,278),(440,213),(459,287),(521,161),(691,201)],
                            #CURRENT_DIR / 'files' / 'five-faces.png': [(221,103),(304,251),(391,222),(469,311),(598,296)],
                            #CURRENT_DIR / 'files' / 'five-faces.jpg': [(219, 105), (300, 252), (392, 220), (469, 309), (600, 294)],
                            #CURRENT_DIR / 'files' / 'two-faces.png': [(809,534),(1165,600)],
                            #CURRENT_DIR / 'files' / 'two-faces.jpg': [(354,232),(505,258)],
                            #CURRENT_DIR / 'files' / 'three-people.png': [(740,598),(906,520),(1045,548)],
                            #CURRENT_DIR / 'files' / 'three-people.jpg': [(315,259),(386,219),(448,233)],
                            #CURRENT_DIR / 'files' / 'four-people.png': [(650,459),(783,444),(996,469),(1163,457)],
                            #CURRENT_DIR / 'files' / 'four-people.jpg': [(275,196), (332,188), (421,197), (495,188)]
                            }

    def find_face(dict_faces_and_noses):
        dict_of_thresholds = {}
        for picture in dict_faces_and_noses:
            im = imageio.imread(picture)
            img = _preprocess_img(im)
            for x in range(1, 10):
                for y in range(1, 10):
                    for z in range(1, 10):
                        print(f'{picture.name}, {(x,y,z)}')

                        box = _get_bounding_boxes_for_threshold(img, [(x * 0.1), (y * 0.1), (z * 0.1)])
                        if len(box) == len(dict_faces_and_noses[picture]):
                            for box_dim in box:
                                xmin = box_dim[0]
                                ymin = box_dim[1]
                                xmax = box_dim[2]
                                ymax = box_dim[3]
                                for nose in range(len(dict_faces_and_noses[picture])):
                                    if xmin < dict_faces_and_noses[picture][nose][0] < xmax and ymin < dict_faces_and_noses[picture][nose][1] < ymax:
                                        if (x, y, z) not in dict_of_thresholds:
                                            dict_of_thresholds[x, y, z] = 1
                                        else:
                                            dict_of_thresholds[x, y, z] += 1

                        list_of_thresholds = sorted(dict_of_thresholds.items(), key=itemgetter(1), reverse=True)
                        print(f'Top threshold: {list_of_thresholds[0] if list_of_thresholds else None}')

        list_of_thresholds = sorted(dict_of_thresholds.items(), key=itemgetter(1), reverse=True)
        return _calculate_threshold(list_of_thresholds) if list_of_thresholds else None

    print(find_face(dict_faces_and_noses))




