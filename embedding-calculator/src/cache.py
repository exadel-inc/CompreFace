"""
This file should contain all objects that are cached in-memory between requests
"""
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
