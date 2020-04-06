import logging
import pickle
import random
from collections import namedtuple

from sample_images.annotations import SAMPLE_IMAGES
from src.logging_ import init_runtime
from src.services.facescan.scanner.facescanners import FaceScanners
from src.services.facescan.scanner.test.calculate_errors import calculate_errors
from src.services.utils.pyutils import get_dir

CURRENT_DIR = get_dir(__file__)
Score = namedtuple('Score', 'cost args')


class Facenet2018ThresholdOptimization:
    def __init__(self):
        self.scanner = FaceScanners.Facenet2018()
        self.dataset = SAMPLE_IMAGES
        logging.info(f'dataset: {[r.image_name for r in self.dataset]}')
        logging.getLogger('src.services.facescan.scanner.test.calculate_errors').setLevel(logging.WARNING)
        logging.getLogger('src.services.facescan.scanner.facenet.facenet').setLevel(logging.INFO)

    def cost(self, x=None):
        if x:
            self.scanner.threshold_a, self.scanner.threshold_b, self.scanner.threshold_c = tuple(x)
        return calculate_errors(self.scanner, self.dataset)


class RandomOptimizer:
    def __init__(self, task, arg_count, arg_range, checkpoint_filename, checkpoint_n, iter_n):
        self._task = task
        self._arg_count = arg_count
        self._arg_range = arg_range
        self._checkpoint_filename = checkpoint_filename
        self._checkpoint_n = checkpoint_n
        self._iter_n = iter_n
        self.scores = []

    def _get_random_args(self):
        return [random.uniform(*self._arg_range) for _ in range(self._arg_count)]

    def _checkpoint(self):
        self.scores = sorted(self.scores, key=lambda x: x.cost)
        logging.debug(f"Best: {self.scores[0].cost} <- {tuple(self.scores[0].args)}")
        with (CURRENT_DIR / self._checkpoint_filename).open('wb') as file_:
            pickle.dump(self.scores, file_, protocol=pickle.HIGHEST_PROTOCOL)
        logging.debug(f"Saved scores to '{self._checkpoint_filename}'.")

    def optimize(self):
        logging.info(f"Init cost: {self._task.cost()}")
        try:
            for i, args in enumerate(self._get_random_args() for _ in range(self._iter_n)):
                cost = task.cost(args)
                self.scores.append(Score(cost, args))
                logging.debug(f"{cost} <- {tuple(args)}")
                if i % self._checkpoint_n == 0:
                    self._checkpoint()
        except Exception as e:
            self._checkpoint()
            raise e from None


if __name__ == '__main__':
    init_runtime()
    task = Facenet2018ThresholdOptimization()
    optimizer = RandomOptimizer(task, arg_count=3, arg_range=(0.5, 1), checkpoint_filename='checkpoint.pickle',
                                checkpoint_n=1, iter_n=1000000)
    optimizer.optimize()
