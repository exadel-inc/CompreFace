"""
This file should contain all objects that are cached in-memory between requests
"""
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

from typing import Type

from src.constants import ENV
from src.services.facescan.scanner.facescanners import id_2_face_scanner_cls
from src.services.utils.pyutils import run_once, run_once_fork_safe


@run_once
def get_scanner():
    from src.services.facescan.scanner.facescanner import FaceScanner
    face_scanner_cls: Type[FaceScanner] = id_2_face_scanner_cls[ENV.SCANNER]
    return face_scanner_cls()


@run_once_fork_safe  # Because PyMongo is not fork-safe
def get_storage():
    from src.services.storage.mongo_storage import MongoStorage
    return MongoStorage(host=ENV.MONGODB_HOST, port=ENV.MONGODB_PORT)


@run_once
def get_training_task_manager():
    from src.services.async_task_manager.async_task_manager import AsyncTaskManager
    from src.services.train_classifier import train_and_save_classifier_async
    return AsyncTaskManager(train_and_save_classifier_async)
