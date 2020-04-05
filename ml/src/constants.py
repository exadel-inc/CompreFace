import json

from src.services.utils.pyutils import get_env


class ENV:
    ML_PORT = int(get_env('ML_PORT', '3000'))
    MONGO_HOST = get_env('MONGO_HOST', 'mongo')
    MONGO_PORT = int(get_env('MONGO_PORT', '27017'))
    MONGO_DBNAME = get_env('MONGO_DBNAME', 'efrs_db')
    IS_DEV_ENV = get_env('FLASK_ENV', '') == 'development'
    IMG_LENGTH_LIMIT = int(get_env('IMG_LENGTH_LIMIT', '1000'))

    @classmethod
    def __str__(cls):
        return json.dumps({key: cls.__dict__[key] for key in cls.__dict__.keys() if not key.startswith('_')}, indent=4)
