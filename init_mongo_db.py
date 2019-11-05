#!/usr/bin/env python3
from main import ROOT_DIR
from src.api.controller import init_runtime
from src.pyutils.raises import raises
from src.storage.constants import EMBEDDING_CALCULATOR_MODEL_FILENAME
from src.storage.exceptions import NoFileFoundInDatabaseError
from src.storage.storage import get_storage

EMBEDDING_CALCULATOR_MODEL_FILEPATH = ROOT_DIR / 'db_data' / EMBEDDING_CALCULATOR_MODEL_FILENAME


def stdout(msg):
    print(f'[init-mongo-db] {msg}')


def upload_calculator_model_file():
    filepath = EMBEDDING_CALCULATOR_MODEL_FILEPATH
    filename = EMBEDDING_CALCULATOR_MODEL_FILENAME
    stdout(f'Saving "{filename}" to Mongo database...')
    with filepath.open('rb') as file:
        bytes_data = file.read()
    get_storage().save_file(filename, bytes_data)
    stdout(f'Successfully saved "{filename}" to Mongo database.')


def is_database_initialized():
    return not raises(NoFileFoundInDatabaseError, lambda: get_storage().get_file(EMBEDDING_CALCULATOR_MODEL_FILENAME))


def init_mongo_db():
    stdout('Starting...')
    if is_database_initialized():
        stdout('Database is already initialized, exiting.')
        return

    init_runtime()
    upload_calculator_model_file()
    stdout('Database has been successfully initialized, exiting.')


if __name__ == '__main__':
    init_mongo_db()
