import json
import os


class ENV:
    ML_URL = os.environ.get('ML_URL', '') or 'http://localhost:3000'
    MONGO_HOST = os.environ.get('MONGO_HOST', '') or 'mongo'
    MONGO_PORT = int(os.environ.get('MONGO_PORT', '') or '27017')
    MONGO_DBNAME = os.environ.get('MONGO_DBNAME', '') or 'efrs_tmp_db'

    CONNECT_TIMEOUT_S = 5
    READ_TIMEOUT_S = 30
    AVAILABLE_SERVICE_TIMEOUT_S = 8
    TRAINING_TIMEOUT_S = 30

    @classmethod
    def __str__(cls):
        return json.dumps({key: cls.__dict__[key] for key in cls.__dict__.keys() if not key.startswith('_')}, indent=4)
