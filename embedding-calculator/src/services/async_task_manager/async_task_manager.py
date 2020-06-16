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
from abc import ABC, abstractmethod
from enum import Enum, auto
from multiprocessing import Process
from typing import Dict, Callable

from src.exceptions import ClassifierIsAlreadyTrainingError

ApiKey = str


class TaskStatus(Enum):
    BUSY = auto()
    IDLE_LAST_NONE = auto()
    IDLE_LAST_OK = auto()
    IDLE_LAST_ERROR = auto()


class TrainingTaskManagerBase(ABC):
    @abstractmethod
    def get_status(self, api_key) -> TaskStatus:
        raise NotImplementedError

    @abstractmethod
    def start_training(self, api_key, force=False):
        raise NotImplementedError

    @abstractmethod
    def abort_training(self, api_key):
        raise NotImplementedError


class AsyncTaskManager(TrainingTaskManagerBase):
    def __init__(self, task_fun: Callable[[ApiKey], None]):
        self._dict: Dict[ApiKey, 'Process'] = {}
        self._train_fun = task_fun

    def get_status(self, api_key) -> TaskStatus:
        if api_key not in self._dict:
            return TaskStatus.IDLE_LAST_NONE
        process = self._dict[api_key]

        if process.is_alive():
            return TaskStatus.BUSY

        if process.exitcode != 0:
            return TaskStatus.IDLE_LAST_ERROR

        return TaskStatus.IDLE_LAST_OK

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
        logging.warning("Forcefully aborting async task")
        self._dict[api_key].terminate()


class SyncTaskManager(TrainingTaskManagerBase):
    """Helper class for debugging purposes"""

    def __init__(self, task_fun: Callable[[ApiKey], None]):
        self._train_fun = task_fun

    def get_status(self, api_key) -> TaskStatus:
        return TaskStatus.IDLE

    def start_training(self, api_key, force=False):
        self._train_fun(api_key)

    def abort_training(self, api_key):
        pass  # Not tasks are ever running in background, so aborting is not relevant
