import logging
import time
from collections import namedtuple

from src.services.facescan.optimize.results_storage import ResultsStorage
from src.services.utils.pyutils import get_current_dir

CURRENT_DIR = get_current_dir(__file__)
Score = namedtuple('Score', 'cost args')


class Optimizer:
    def __init__(self, task, results_storage: ResultsStorage, checkpoint_every_s):
        self._task = task
        self._results_storage = results_storage
        self._checkpoint_every_s = checkpoint_every_s

    def optimize(self, get_new_args_iterator):
        logging.info(f"Init cost: {self._task.cost()}")
        last_checkpoint_s = time.time()
        try:
            for args in get_new_args_iterator:
                cost = self._task.cost(args)
                self._results_storage.add_score(Score(cost, args))
                logging.debug(f"{cost} <- {tuple(args)}")
                if (time.time() - last_checkpoint_s) > self._checkpoint_every_s:
                    self._results_storage.save()
                    last_checkpoint_s = time.time()
        except Exception as e:
            self._results_storage.save()
            raise e from None
