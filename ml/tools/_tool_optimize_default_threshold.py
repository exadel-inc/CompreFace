import pickle
import random
from collections import namedtuple

import imageio

from sample_images import IMG_DIR
from src.cache import get_storage, get_scanner
from src.services.facescan.scanner.facenet.facenet import Facenet2018
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.facescan.scanner.facescanners import ALL_SCANNERS
from src.services.storage.mongo_storage import MongoStorage
from src.services.utils.pyutils import get_dir

EXPERIMENT_SIZE = 3000
CURRENT_DIR = get_dir(__file__)


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
    IMG_DIR / 'four-people.jpg': [(275, 196), (332, 188), (421, 197), (495, 188)],
    IMG_DIR / 'personD-img1.jpg': [(2109, 2261)],
    IMG_DIR / 'personD-img2.jpg': [(3210, 1382)],
    IMG_DIR / 'personD-img3.jpg': [(2146, 2424)],
    IMG_DIR / 'personD-img4.jpg': [(1312, 1969)]
}

CalcResult = namedtuple('Score', 'points_outside_one_box boxes_with_not_one_point total_boxes total_points')


def point_inside_bounding_box(bounding_box, point) -> bool:
    x_min = int(bounding_box[0])
    y_min = int(bounding_box[1])
    x_max = int(bounding_box[2])
    y_max = int(bounding_box[3])
    point_x = point[0]
    point_y = point[1]
    return x_min <= point_x <= x_max and y_min <= point_y <= y_max


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


def calc_score_for_image(img, threshold, nose_locations, backend) -> CalcResult:
    scanner: FaceScanner = get_scanner(backend)
    storage: MongoStorage = get_storage()
    detect_face_result = scanner.scan(img, detection_threshold=threshold)
    bounding_boxes = list(detect_face_result[0][:, 0:4])
    return calc_score_for_bounding_boxes(bounding_boxes, nose_locations)


Score = namedtuple('Score', 'errors boxes_with_not_one_point points_outside_one_box')
ThresholdScore = namedtuple('ThresholdScore', 'threshold score')


def save_state():
    with (CURRENT_DIR / 'threshold_scores_facenet.pickle').open('wb') as file_:
        pickle.dump(threshold_scores_facenet, file_, protocol=pickle.HIGHEST_PROTOCOL)
    with (CURRENT_DIR / 'threshold_scores_insightface.pickle').open('wb') as file_:
        pickle.dump(threshold_scores_insightface, file_, protocol=pickle.HIGHEST_PROTOCOL)
    print('Saved state.')


if __name__ == "__main__":
    thresholds = ([get_random_threshold() for _ in range(EXPERIMENT_SIZE)])

    threshold_scores_facenet = []
    threshold_scores_insightface = []
    for i, threshold in enumerate(thresholds):
        scores_facenet = []
        scores_insightface = []
        for img_filepath, nose_locations in DATASET.items():
            img = imageio.imread(img_filepath)
            for backend in ALL_SCANNERS:
                score = []
                while True:
                    try:
                        score = calc_score_for_image(img, list(threshold), nose_locations, backend)
                        # print(f' - {img_filepath.name}: {score}')
                        if backend == Facenet2018:
                            scores_facenet.append(score)
                        else:
                            scores_insightface.append(score)

                    except Exception as e:
                        print(str(e))
                        save_state()
                        if backend == Facenet2018:
                            print(
                                f'Top score (out of {len(threshold_scores_facenet)}): {threshold_scores_facenet[0] if len(threshold_scores_facenet) else None}')
                        else:
                            print(
                                f'Top score (out of {len(threshold_scores_insightface)}): {threshold_scores_insightface[0] if len(threshold_scores_insightface) else None}')


                    else:
                        continue

        total_score_facenet = Score(errors=sum(score.boxes_with_not_one_point + score.points_outside_one_box
                                               for score in scores_facenet),
                                    boxes_with_not_one_point=sum(score.boxes_with_not_one_point
                                                                 for score in scores_facenet),
                                    points_outside_one_box=sum(score.points_outside_one_box
                                                               for score in scores_facenet))

        total_score_insightface = Score(errors=sum(score.boxes_with_not_one_point + score.points_outside_one_box
                                                   for score in scores_insightface),
                                        boxes_with_not_one_point=sum(score.boxes_with_not_one_point
                                                                     for score in scores_insightface),
                                        points_outside_one_box=sum(score.points_outside_one_box
                                                                   for score in scores_insightface))

        threshold_score_facenet = ThresholdScore(threshold, total_score_facenet)
        print(f'Facenet: ')
        print(f'{threshold_score_facenet}')
        threshold_scores_facenet.append(threshold_score_facenet)
        threshold_scores = sorted(threshold_scores, key=lambda x: x.score.errors)
        print(f'Top score (out of {len(threshold_scores)}): {threshold_scores[0] if threshold_scores else None}')

        if i % 50 == 0:
            save_state()

        threshold_score_insightface = ThresholdScore(threshold, total_score_insightface)
        print(f'Insightface: ')
        print(f'{threshold_score_insightface}')
        threshold_scores.append(threshold_score_insightface)
        threshold_scores = sorted(threshold_scores, key=lambda x: x.score.errors)
        print(f'Top score (out of {len(threshold_scores)}): {threshold_scores[0] if threshold_scores else None}')

        if i % 50 == 0:
            save_state()

    print("\nLeaderboard for Facenet:")
    for i, threshold_score in enumerate(threshold_scores, start=1):
        print(f'#{i}. {threshold_score}')
        if i == 50:
            break

    save_state()  # Saving optimization results for further analysis
    # Note: Default threshold has to be selected and updated manually
