#  Copyright (c) 2020 the original author or authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

import logging
from collections import namedtuple
from pathlib import Path

import numpy as np

from src.constants import ENV_MAIN, LOGGING_LEVEL
from src.init_runtime import init_runtime
from src.services.facescan.scanner.test.calculate_errors import calculate_missed_boxes, calculate_missed_noses
from src.services.imgtools.read_img import read_img
from src.services.utils.pyutils import get_current_dir
from tools._save_img import save_img
from tools.benchmark_detection.constants import ENV
from tools.benchmark_detection.simple_stats import SimpleStats
from tools.constants import get_scanner

AnnotatedImg = namedtuple('AnnotatedImg', 'img img_name noses')
TMP_DIR = get_current_dir(__file__) / 'tmp'
ERR_IMG_DIR = TMP_DIR / 'error-images'
logger = logging.getLogger(__name__)


def _get_image(img_name):
    image_path = TMP_DIR / 'originalPics' / Path(f'{img_name}.jpg')
    return read_img(image_path) if not ENV.DRY_RUN else np.zeros((1, 1, 3))


def _get_noses(annotation_file):
    ellipse_count = int(next(annotation_file))
    noses = []
    for _ in range(ellipse_count):
        annotation_line_parts = next(annotation_file).split()
        ellipse_center_xy = round(float(annotation_line_parts[3])), round(float(annotation_line_parts[4]))
        noses.append(ellipse_center_xy)
    return noses


def _get_annotated_images():
    annotation_file_paths = sorted(TMP_DIR.glob('FDDB-folds/FDDB-fold-*-ellipseList.txt'))
    for annotation_file_path in annotation_file_paths:
        with annotation_file_path.open('r') as annotation_file:
            for img_name in annotation_file:
                img_name = img_name.strip()
                img = _get_image(img_name)
                noses = _get_noses(annotation_file)
                yield AnnotatedImg(img, img_name, noses)


if __name__ == '__main__':
    init_runtime(logging_level=LOGGING_LEVEL)
    logger.info(ENV.to_json() if ENV_MAIN.IS_DEV_ENV else ENV.to_str())
    logging.getLogger('src.services.facescan.scanner').setLevel(logging.INFO)
    if ENV.SAVE_IMG_ON_ERROR:
        ERR_IMG_DIR.mkdir(parents=True, exist_ok=True)

    annotated_images = list(_get_annotated_images())
    img_count, face_count = len(annotated_images), sum(len(k.noses) for k in annotated_images)
    print(f"LFW Face Detection dataset: {face_count} faces in a set of {img_count} images")

    for scanner_name in ENV.SCANNERS:
        scanner = get_scanner(scanner_name)
        simple_stats = SimpleStats(scanner_name)

        for annotated_image in annotated_images:
            img, noses, img_name = annotated_image.img, annotated_image.noses, annotated_image.img_name
            boxes = scanner.find_faces(img)
            missed_boxes, missed_noses = calculate_missed_boxes(boxes, noses), calculate_missed_noses(boxes, noses)
            simple_stats.add(total_boxes=len(boxes), total_noses=len(noses),
                             total_missed_boxes=missed_boxes, total_missed_noses=missed_noses)
            if (missed_boxes or missed_noses) and ENV.SAVE_IMG_ON_ERROR:
                filepath = ERR_IMG_DIR / f'{img_name}_{scanner_name}.png'.replace('/', '_')
                save_img(img, boxes, noses, filepath)
            logging.debug(simple_stats.__str__(f'{scanner_name} {img_name}'))
        print(f'\n{scanner_name} detected {simple_stats.total_boxes} faces.')
        print(simple_stats)
