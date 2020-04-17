import logging
import pickle
import random
import time
from collections import namedtuple

from src.services.utils.pyutils import get_dir

CURRENT_DIR = get_dir(__file__)
Score = namedtuple('Score', 'cost args')


class RandomOptimizer:
    def __init__(self, task, arg_count, arg_range, checkpoint_filename, checkpoint_every_s):
        self._task = task
        self._arg_count = arg_count
        self._arg_range = arg_range
        self._checkpoint_filename = checkpoint_filename
        self._checkpoint_every_s = checkpoint_every_s
        self._iterations = 0
        self.scores = []

    def _get_random_args(self):
        return [random.uniform(*self._arg_range) for _ in range(self._arg_count)]

    def _checkpoint(self):
        self.scores = sorted(self.scores, key=lambda x: x.cost)[:100]
        logging.debug(f"Best out of {self._iterations}: Cost = {self.scores[0].cost} <- {tuple(self.scores[0].args)}")
        with (CURRENT_DIR / self._checkpoint_filename).open('wb') as file_:
            pickle.dump(self.scores, file_, protocol=pickle.HIGHEST_PROTOCOL)
        logging.debug(f"Saved top 100 scores to '{self._checkpoint_filename}'.")

    def optimize(self):
        logging.info(f"Init cost: {self._task.cost()}")
        last_checkpoint_s = time.time()
        try:
            while True:
                args = self._get_random_args()
                cost = self._task.cost(args)
                self.scores.append(Score(cost, args))
                logging.debug(f"{cost} <- {tuple(args)}")
                if (time.time() - last_checkpoint_s) > self._checkpoint_every_s:
                    self._checkpoint()
                    last_checkpoint_s = time.time()
                self._iterations += 1
        except Exception as e:
            self._checkpoint()
            raise e from None
