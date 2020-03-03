import os
import pickle
import random
from collections import namedtuple
from pathlib import Path

import imageio

from src import _pyutils
from src.scan_faces._detector._lib.align import detect_face
from src.scan_faces._detector.constants import SCALE_FACTOR, FACE_MIN_SIZE
from src.scan_faces._detector.detector import _face_detection_nets, _preprocess_img

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
IMG_DIR = CURRENT_DIR / '_files'

EXPERIMENT_SIZE = 3000


def get_random_threshold():
    return [random.uniform(0.5, 1) for _ in range(3)]


DATASET = {
    IMG_DIR / 'four-faces.png': [(746, 568), (495, 487), (1046, 548), (1485, 578)],
    IMG_DIR / 'four-faces.jpg': [(212, 209), (319, 238), (443, 234), (629, 248)],
    IMG_DIR / 'four-plus-one-faces.png': [(660, 630), (808, 618), (898, 665), (972, 513), (1153, 576)],
    IMG_DIR / 'four-plus-one-faces.jpg': [(281, 268), (344, 261), (383, 283), (413, 221), (494, 248)],
    IMG_DIR / 'five-people.png': [(225, 666), (484, 625), (956, 419), (1417, 638), (1617, 716)],
    IMG_DIR / 'five-people.jpg': [(95, 283), (207, 262), (407, 175), (605, 270), (691, 305)],
    IMG_DIR / 'six-faces.png': [(384, 537), (632, 631), (823, 666), (1060, 636), (1306, 623), (1494, 590)],
    IMG_DIR / 'six-faces.jpg': [(164, 229), (269, 269), (352, 282), (453, 269), (557, 263), (635, 250)],
    IMG_DIR / 'eight-faces.png': [(461, 651), (612, 686), (619, 400), (836, 663), (1034, 499), (1064, 676), (1217, 386),
                                  (1613, 479)],
    IMG_DIR / 'eight-faces.jpg': [(194, 277), (262, 169), (260, 292), (357, 278), (440, 213), (459, 287), (521, 161),
                                  (691, 201)],
    IMG_DIR / 'five-faces.png': [(221, 103), (304, 251), (391, 222), (469, 311), (598, 296)],
    IMG_DIR / 'five-faces.jpg': [(219, 105), (300, 252), (392, 220), (469, 309), (600, 294)],
    IMG_DIR / 'two-faces.png': [(809, 534), (1165, 600)],
    IMG_DIR / 'two-faces.jpg': [(354, 232), (505, 258)],
    IMG_DIR / 'three-people.png': [(740, 598), (906, 520), (1045, 548)],
    IMG_DIR / 'three-people.jpg': [(315, 259), (386, 219), (448, 233)],
    IMG_DIR / 'four-people.png': [(650, 459), (783, 444), (996, 469), (1163, 457)],
    IMG_DIR / 'four-people.jpg': [(275, 196), (332, 188), (421, 197), (495, 188)]
}

CalcResult = namedtuple('Score', 'points_outside_one_box boxes_with_not_one_point total_boxes total_points')


def point_inside_bounding_box(bounding_box, point) -> bool:
    xmin = int(bounding_box[0])
    ymin = int(bounding_box[1])
    xmax = int(bounding_box[2])
    ymax = int(bounding_box[3])
    point_x = point[0]
    point_y = point[1]
    return xmin <= point_x <= xmax and ymin <= point_y <= ymax


def filter_only_noses_with_max_one_bounding_box(bounding_boxes, nose_locations):
    return [nose_location for nose_location in nose_locations
            if sum(point_inside_bounding_box(bounding_box, nose_location)
                   for bounding_box in bounding_boxes) <= 1]


def calc_score_for_bounding_boxes(bounding_boxes, points) -> CalcResult:
    """
    >>> calc_score_for_bounding_boxes([(100,500,150,550)], [(125, 525)])
    Score(points_outside_one_box=0, boxes_with_not_one_point=0, total_boxes=1, total_points=1)
    >>> calc_score_for_bounding_boxes([(100,500,150,550)], [(1125, 1525)])
    Score(points_outside_one_box=1, boxes_with_not_one_point=1, total_boxes=1, total_points=1)
    >>> calc_score_for_bounding_boxes([(100,500,150,550), (100,500,150,550)], [(125, 525)])
    Score(points_outside_one_box=1, boxes_with_not_one_point=0, total_boxes=2, total_points=1)
    >>> calc_score_for_bounding_boxes([(100,500,150,550)], [(125, 525), (125, 525)])
    Score(points_outside_one_box=0, boxes_with_not_one_point=1, total_boxes=1, total_points=2)
    """
    points_outside_one_box, boxes_with_not_one_point = 0, 0

    for point in points:
        boxes_with_point_inside_count = sum(point_inside_bounding_box(bounding_box, point)
                                            for bounding_box in bounding_boxes)
        if boxes_with_point_inside_count != 1:
            points_outside_one_box += 1

    for bounding_box in bounding_boxes:
        points_inside_box_count = sum(point_inside_bounding_box(bounding_box, point)
                                      for point in points)
        if points_inside_box_count != 1:
            boxes_with_not_one_point += 1

    return CalcResult(points_outside_one_box=points_outside_one_box,
                      boxes_with_not_one_point=boxes_with_not_one_point,
                      total_boxes=len(bounding_boxes),
                      total_points=len(points))


def calc_score_for_image(img, threshold, nose_locations) -> CalcResult:
    fdn = _face_detection_nets()
    detect_face_result = detect_face.detect_face(img, FACE_MIN_SIZE, fdn.pnet, fdn.rnet, fdn.onet, threshold,
                                                 SCALE_FACTOR)
    bounding_boxes = list(detect_face_result[0][:, 0:4])
    return calc_score_for_bounding_boxes(bounding_boxes, nose_locations)


@_pyutils.cached
def open_image(img_filepath):
    img = imageio.imread(img_filepath)
    img = _preprocess_img(img)
    return img


Score = namedtuple('Score', 'errors boxes_with_not_one_point points_outside_one_box')
ThresholdScore = namedtuple('ThresholdScore', 'threshold score')


def save_state():
    with (CURRENT_DIR / 'threshold_scores.pickle').open('wb') as file_:
        pickle.dump(threshold_scores, file_, protocol=pickle.HIGHEST_PROTOCOL)
    print('Saved state.')


if __name__ == "__main__":
    thresholds = ([get_random_threshold() for _ in range(EXPERIMENT_SIZE)])

    threshold_scores = []
    for i, threshold in enumerate(thresholds):
        scores = []
        for img_filepath, nose_locations in DATASET.items():
            img = open_image(img_filepath)

            while True:
                try:
                    score = calc_score_for_image(img, list(threshold), nose_locations)
                except Exception as e:
                    print(str(e))
                    save_state()
                    print(
                        f'Top score (out of {len(threshold_scores)}): {threshold_scores[0] if len(threshold_scores) else None}')
                else:
                    break

            # print(f' - {img_filepath.name}: {score}')
            scores.append(score)
        total_score = Score(errors=sum(score.boxes_with_not_one_point + score.points_outside_one_box
                                       for score in scores),
                            boxes_with_not_one_point=sum(score.boxes_with_not_one_point
                                                         for score in scores),
                            points_outside_one_box=sum(score.points_outside_one_box
                                                       for score in scores))
        threshold_score = ThresholdScore(threshold, total_score)
        print(f'{threshold_score}')
        threshold_scores.append(threshold_score)
        threshold_scores = sorted(threshold_scores, key=lambda x: x.score.errors)
        print(f'Top score (out of {len(threshold_scores)}): {threshold_scores[0] if threshold_scores else None}')

        if i % 50 == 0:
            save_state()

    print("\nLeaderboard:")
    for i, threshold_score in enumerate(threshold_scores, start=1):
        print(f'#{i}. {threshold_score}')
        if i == 50:
            break

    save_state()  # Saving optimization results for further analysis
    # Note: Default threshold has to be selected and updated manually
