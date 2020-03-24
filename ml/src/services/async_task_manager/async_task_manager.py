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
        elif self.get_status(api_key) == TaskStatus.BUSY:
            raise ClassifierIsAlreadyTrainingError

        process = Process(target=self._train_fun, daemon=True, args=(api_key,))
        process.start()
        self._dict[api_key] = process

    def abort_training(self, api_key):
        if self.get_status(api_key) != TaskStatus.BUSY:
            return
        logging.warning(f"Forcefully aborting async task")
        self._dict[api_key].terminate()
