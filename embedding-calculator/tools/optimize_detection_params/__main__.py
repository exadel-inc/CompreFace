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

import itertools
import logging
import random
from collections import namedtuple
from functools import lru_cache

from sample_images import IMG_DIR
from sample_images.annotations import SAMPLE_IMAGES
from src.constants import ENV_MAIN, LOGGING_LEVEL
from src.init_runtime import init_runtime
from src.services.facescan.plugins.facenet.facenet import FaceDetector
from src.services.facescan.scanner.test.calculate_errors import calculate_errors
from src.services.imgtools.read_img import read_img
from src.services.utils.pyutils import get_current_dir, Constants, get_env_split
from tools.optimize_detection_params.optimizer import Optimizer
from tools.optimize_detection_params.results_storage import ResultsStorage

CURRENT_DIR = get_current_dir(__file__)
Score = namedtuple('Score', 'cost args')

cached_read_img = lru_cache(maxsize=None)(read_img)

logger = logging.getLogger(__name__)


class ENV(Constants):
    LOGGING_LEVEL_NAME = ENV_MAIN.LOGGING_LEVEL_NAME
    IMG_NAMES = get_env_split('IMG_NAMES', ' '.join([i.img_name for i in SAMPLE_IMAGES]))


class Facenet2018DetectionThresholdOptimization:
    def __init__(self):
        self.arg_count = 4
        self.detector = FaceDetector()
        self.dataset = [row for row in SAMPLE_IMAGES if row.img_name in ENV.IMG_NAMES]
        logging.getLogger('src.services.facescan.scanner.test.calculate_errors').setLevel(logging.WARNING)
        logging.getLogger('src.services.facescan.scanner.facenet.facenet').setLevel(logging.INFO)

    def cost(self, new_x=None):
        if new_x:
            (self.detector.det_prob_threshold,
             self.detector.threshold_a,
             self.detector.threshold_b,
             self.detector.threshold_c) = tuple(new_x)

        total_errors = 0
        for row in self.dataset:
            img = cached_read_img(IMG_DIR / row.img_name)
            boxes = self.detector.find_faces(img)
            errors = calculate_errors(boxes, row.noses)
            total_errors += errors
        return total_errors


def get_plausible_thresholds_iterator(arg_count):
    one_arg_values = [0.01, 0.1, 0.2, 0.3, 0.4, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 0.99]
    all_arg_values = list(itertools.product(one_arg_values, repeat=arg_count))
    random.shuffle(all_arg_values)
    return all_arg_values


def random_thresholds_generator(arg_count):
    while True:
        yield [random.uniform(0, 1) for _ in range(arg_count)]


if __name__ == '__main__':
    init_runtime(logging_level=LOGGING_LEVEL)
    logger.info(ENV.to_json() if ENV_MAIN.IS_DEV_ENV else ENV.to_str())

    task = Facenet2018DetectionThresholdOptimization()
    threshold_iterators = [
        get_plausible_thresholds_iterator(task.arg_count),
        random_thresholds_generator(task.arg_count)
    ]

    storage = ResultsStorage()
    optimizer = Optimizer(task, storage, checkpoint_every_s=120)
    for threshold_iterator in threshold_iterators:
        optimizer.optimize(threshold_iterator)
