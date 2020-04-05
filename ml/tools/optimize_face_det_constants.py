import logging
import pickle
from abc import ABC, abstractmethod
from collections import namedtuple
import random

import numpy as np

from sample_images.annotations import SAMPLE_IMAGES
from src.logging_ import init_runtime
from src.services.facescan.scanner.facescanners import FaceScanners
from src.services.facescan.scanner.test.calculate_errors import calculate_errors
from src.services.utils.pyutils import get_dir

CURRENT_DIR = get_dir(__file__)


class OptimizationTask(ABC):
    @property
    @abstractmethod
    def x0(self):
        raise NotImplementedError

    @abstractmethod
    def cost(self, x):
        pass

    @abstractmethod
    def bounds(self, x_new, **kwargs):
        pass

    @abstractmethod
    def callback(self, x, f):
        pass


class Facenet2018ThresholdOptimization(OptimizationTask):
    def __init__(self):
        self.scanner = FaceScanners.Facenet2018()
        self.dataset = [r for r in SAMPLE_IMAGES if r.image_name in ['01.A.jpg', '06.A.jpg', '08.B.jpg']]
        self.dataset = [r for r in SAMPLE_IMAGES if 'A' in r.image_name]
        self.dataset = SAMPLE_IMAGES
        logging.info(f'dataset: {[r.image_name for r in self.dataset]}')
        logging.getLogger('src.services.facescan.scanner.test.calculate_errors').setLevel(logging.WARNING)
        logging.getLogger('src.services.facescan.scanner.facenet.facenet').setLevel(logging.INFO)

    @property
    def x0(self):
        s = FaceScanners.Facenet2018
        return np.array([s.DEFAULT_THRESHOLD_A, s.DEFAULT_THRESHOLD_B, s.DEFAULT_THRESHOLD_C])

    def cost(self, x=None):
        if x:
            self.scanner.threshold_a, self.scanner.threshold_b, self.scanner.threshold_c = tuple(x)
        return calculate_errors(self.scanner, self.dataset)

    def bounds(self, x_new, **kwargs):
        return (0 <= x_new).all() and (x_new <= 1).all()

    def callback(self, x, f):
        logging.debug(f"{f} <- {tuple(x)}")


def get_random_threshold():
    return [random.uniform(0.5, 1) for _ in range(3)]


def checkpoint(scores):
    scores = sorted(scores, key=lambda x: x.cost)
    logging.debug(f"Best: {scores[0].cost} <- {tuple(scores[0].args)}")

    with (CURRENT_DIR / 'scores.pickle').open('wb') as file_:
        pickle.dump(scores, file_, protocol=pickle.HIGHEST_PROTOCOL)
    logging.debug('Saved state.')

    return scores


Score = namedtuple('Score', 'cost args')

if __name__ == '__main__':
    init_runtime()
    scores = []
    task = Facenet2018ThresholdOptimization()
    logging.info(f"Init cost: {task.cost()}")
    try:
        for i, args in enumerate(get_random_threshold() for _ in range(10000)):
            cost = task.cost(args)
            scores.append(Score(cost, args))
            task.callback(x=args, f=cost)
            if i % 50 == 0:
                scores = checkpoint(scores)
    except Exception as e:
        checkpoint(scores)
        raise e from None
