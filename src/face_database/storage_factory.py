import os

from src.face_database._mongo_helper import MongoStorage
from src.face_database._mysql_helper import MySQLStorage


def get_storage():
    if os.environ.get('MYSQL_URL', None):
        model_type = 'mysql'
    else:
        model_type = 'mongo'

    if model_type == 'mongo':
        mongo_host = os.environ.get('MONGO_HOST', 'mongo')
        mongo_port = int(os.environ.get('MONGO_PORT', '27017'))
        return MongoStorage(mongo_host, mongo_port)
    elif model_type == 'mysql':
        url = os.environ.get('MYSQL_URL', 'mysql://root:root@localhost/recognition')
        return MySQLStorage(url)
