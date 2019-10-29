import os

from src import pyutils
from src.storage._mongo_storage import MongoStorage


@pyutils.run_once  # Singleton pattern (don't establish a new connection for each use)
def get_storage():
    return MongoStorage(host=os.environ.get('MONGO_HOST', 'mongo'),
                        port=int(os.environ.get('MONGO_PORT', '27017')))
