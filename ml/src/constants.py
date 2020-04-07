import json

from pymongo import uri_parser

from src.services.utils.pyutils import get_env, first_and_only


class ENV:
    ML_PORT = int(get_env('ML_PORT', '3000'))

    _MONGODB_HOST = get_env('MONGODB_HOST', 'mongodb')
    _MONGODB_PORT = int(get_env('MONGODB_PORT', '27017'))
    _MONGODB_DBNAME = get_env('MONGODB_DBNAME', 'efrs_db')
    MONGODB_URI = get_env('MONGODB_URI', f'mongodb://{_MONGODB_HOST}:{_MONGODB_PORT}/{_MONGODB_DBNAME}')

    _MONGODB_URI_PARSED = uri_parser.parse_uri(MONGODB_URI)
    MONGODB_HOST, MONGODB_PORT = first_and_only(_MONGODB_URI_PARSED['nodelist'])
    MONGODB_DBNAME = _MONGODB_URI_PARSED['database']

    DEFAULT_FACE_SCANNER = get_env('DEFAULT_FACE_SCANNER', 'Facenet2018')
    IMG_LENGTH_LIMIT = int(get_env('IMG_LENGTH_LIMIT', '640'))

    @classmethod
    def __str__(cls):
        return json.dumps({key: cls.__dict__[key] for key in cls.__dict__.keys() if not key.startswith('_')}, indent=4)


DO_SHOW_STACKTRACE_IN_LOGS = True
