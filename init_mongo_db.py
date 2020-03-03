import gridfs
from pymongo import MongoClient

from src import _pyutils
from src._pyutils.raises import raises
from src.init_runtime import init_runtime
from src.scan_faces._embedder.embedder import MODEL_PATH
from src.storage import get_file_from_mongo, save_file_to_mongo
from src.storage.constants import EMBEDDING_CALCULATOR_MODEL_FILENAME, MONGO_EFRS_DATABASE_NAME, MONGO_HOST, MONGO_PORT, \
    CollectionName
from src.storage.exceptions import NoFileFoundInDatabaseError

EMBEDDING_CALCULATOR_MODEL_FILEPATH = MODEL_PATH


@_pyutils.run_once
def files_fs():
    mongo_client = MongoClient(host=MONGO_HOST, port=MONGO_PORT)
    db = mongo_client[MONGO_EFRS_DATABASE_NAME]
    return gridfs.GridFS(db, CollectionName.FILES)


def stdout(msg):
    print(f'[init-mongo-db] {msg}')


def upload_calculator_model_file():
    filepath = EMBEDDING_CALCULATOR_MODEL_FILEPATH
    filename = EMBEDDING_CALCULATOR_MODEL_FILENAME
    stdout(f'Saving "{filename}" to Mongo database...')
    with filepath.open('rb') as file:
        bytes_data = file.read()
    save_file_to_mongo(files_fs(), filename, bytes_data)
    stdout(f'Successfully saved "{filename}" to Mongo database.')


def is_database_initialized():
    return not raises(NoFileFoundInDatabaseError,
                      lambda: get_file_from_mongo(files_fs(), EMBEDDING_CALCULATOR_MODEL_FILENAME))


def init_mongo_db():
    stdout('Starting...')
    if is_database_initialized():
        stdout('Database is already initialized, exiting.')
        return

    upload_calculator_model_file()
    stdout('Database has been successfully initialized, exiting.')


if __name__ == '__main__':
    init_runtime()
    init_mongo_db()
