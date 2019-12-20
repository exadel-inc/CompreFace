from multiprocessing import Process

from toolz import valfilter

from src import pyutils
from src.api.exceptions import ClassifierIsAlreadyTrainingError
from src.face_recognition.classify_embedding.train import train_and_save_model

_api_key_2_train_process = {}


def _update_currently_training_api_keys():
    global _api_key_2_train_process
    _api_key_2_train_process = valfilter(lambda process: process.is_alive(), _api_key_2_train_process)


def _is_training(api_key):
    return api_key in _api_key_2_train_process


def _abort_training(api_key):
    if not _is_training(api_key):
        return
    _api_key_2_train_process[api_key].terminate()
    del _api_key_2_train_process[api_key]


@pyutils.run_first(_update_currently_training_api_keys)
def start_training(api_key, force=False):
    if force:
        _abort_training(api_key)
    elif _is_training(api_key):
        raise ClassifierIsAlreadyTrainingError

    process = Process(target=train_and_save_model, daemon=True, args=[api_key])
    process.start()
    _api_key_2_train_process[api_key] = process


@pyutils.run_first(_update_currently_training_api_keys)
def abort_training(api_key):
    _abort_training(api_key)


@pyutils.run_first(_update_currently_training_api_keys)
def is_training(api_key):
    return _is_training(api_key)
