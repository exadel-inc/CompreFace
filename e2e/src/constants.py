import json
import os


def _get_env(name: str, default: str) -> str:
    return os.environ.get(name, '') or default


class ENV:
    ML_URL = _get_env('ML_URL', 'http://localhost:3000')
    API_KEY = _get_env('API-KEY', 'test-api-key')
    DROP_DB = _get_env('DROP_DB', 'true').lower() in ('true', '1')
    MONGO_HOST = _get_env('MONGO_HOST', 'mongo')
    MONGO_PORT = int(_get_env('MONGO_PORT', '27017'))
    MONGO_DBNAME = _get_env('MONGO_DBNAME', 'efrs_tmp_db')

    CONNECT_TIMEOUT_S = 5
    READ_TIMEOUT_S = 30
    AVAILABLE_SERVICE_TIMEOUT_S = 8
    TRAINING_TIMEOUT_S = 30

    @classmethod
    def __str__(cls):
        return json.dumps({key: cls.__dict__[key] for key in cls.__dict__.keys() if not key.startswith('_')}, indent=4)
