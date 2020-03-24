import logging
from enum import Enum, auto
from multiprocessing import Process
from typing import Dict, Callable

from src.exceptions import ClassifierIsAlreadyTrainingError

# noinspection PyPep8Naming
ApiKey = str


class TaskStatus(Enum):
    BUSY = auto()
    IDLE = auto()
    IDLE_LAST_FAILED = auto()


class AsyncTaskManager:
    def __init__(self, task_fun: Callable[[ApiKey], None]):
        self._dict: Dict[ApiKey, 'Process'] = {}
        self._train_fun = task_fun

    def get_status(self, api_key):
        if api_key not in self._dict:
            return TaskStatus.IDLE
        process = self._dict[api_key]

        if process.is_alive():
            return TaskStatus.BUSY

        if process.exitcode != 0:
            return TaskStatus.IDLE_LAST_FAILED

        del self._dict[api_key]
        return TaskStatus.IDLE

    def start_training(self, api_key, force=False):
        if force:
            self.abort_training(api_key)
        elif not self.is_training(api_key):
            raise ClassifierIsAlreadyTrainingError

        process = Process(target=self._train_fun, daemon=True, args=(api_key,))
        process.start()
        self._dict[api_key] = process

    def abort_training(self, api_key):
        if not self.is_training(api_key):
            return
        logging.warning(f"Forcefully aborting async task")
        self._dict[api_key].terminate()
    #
    # @property
    # def dict(self):
    #     new_dict = {}
    #     for key in self._dict:
    #         process = self._dict[key]
    #         if process.is_alive():
    #             new_dict[key] = process
    #         if process.exitcode != 0:
    #             logger.error('Async process has finished with error')
    #     self._dict = new_dict
    #     return self._dict
