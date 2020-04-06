import json

from src.services.utils.pyutils import get_env


class ENV:
    ML_PORT = int(get_env('ML_PORT', '3000'))
    MONGO_HOST = get_env('MONGO_HOST', 'mongodb')
    MONGO_PORT = int(get_env('MONGO_PORT', '27017'))
    MONGO_DBNAME = get_env('MONGO_DBNAME', 'efrs_db')
    IMG_LENGTH_LIMIT = int(get_env('IMG_LENGTH_LIMIT', '640'))
    DO_SHOW_STACKTRACE_IN_LOGS = True

    @classmethod
    def __str__(cls):
        return json.dumps({key: cls.__dict__[key] for key in cls.__dict__.keys() if not key.startswith('_')}, indent=4)
