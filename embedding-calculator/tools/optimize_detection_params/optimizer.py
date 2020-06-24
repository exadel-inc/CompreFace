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
import time
from collections import namedtuple

from src.services.utils.pyutils import get_current_dir
from tools.optimize_detection_params.results_storage import ResultsStorage

CURRENT_DIR = get_current_dir(__file__)
Score = namedtuple('Score', 'cost args')

logger = logging.getLogger(__name__)


class Optimizer:
    def __init__(self, task, results_storage: ResultsStorage, checkpoint_every_s):
        self._task = task
        self._results_storage = results_storage
        self._checkpoint_every_s = checkpoint_every_s

    def optimize(self, get_new_args_iterator):
        logger.info(f"Init cost: {self._task.cost()}")
        last_checkpoint_s = time.time()
        try:
            for args in get_new_args_iterator:
                cost = self._task.cost(args)
                self._results_storage.add_score(Score(cost, args))
                logger.debug(f"{cost} <- {tuple(args)}")
                if (time.time() - last_checkpoint_s) > self._checkpoint_every_s:
                    self._results_storage.save()
                    last_checkpoint_s = time.time()
        except Exception as e:
            self._results_storage.save()
            raise e from None
