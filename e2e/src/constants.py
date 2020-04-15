import json
import os

from pymongo import uri_parser


def _first_and_only(lst):
    lst = tuple(lst)
    length_lst = len(lst)
    assert length_lst == 1, f"Item count is '{length_lst}' instead of '1'"
    return lst[0]


def _get_env(name: str, default: str) -> str:
    return os.environ.get(name, '') or default


def _is_jsonable(x):
    try:
        json.dumps(x)
        return True
    except TypeError:
        return False


class ENV_E2E:
    _ML_HOST = _get_env('ML_HOST', 'localhost')
    _ML_PORT = int(_get_env('ML_PORT', '3000'))
    ML_URL = _get_env('ML_URL', f'http://{_ML_HOST}:{_ML_PORT}')
    API_KEY = _get_env('API_KEY', 'test-api-key')

    DROP_DB = _get_env('DROP_DB', 'true').lower() in ('true', '1')
    _MONGODB_HOST = _get_env('MONGODB_HOST', 'mongodb')
    _MONGODB_PORT = int(_get_env('MONGODB_PORT', '27017'))
    _MONGODB_DBNAME = _get_env('MONGODB_DBNAME', 'efrs_db')
    MONGODB_URI = _get_env('MONGODB_URI', f'mongodb://{_MONGODB_HOST}:{_MONGODB_PORT}/{_MONGODB_DBNAME}')
    _PARSED_MONGODB_URI = uri_parser.parse_uri(MONGODB_URI)
    MONGODB_HOST, MONGODB_PORT = _first_and_only(_PARSED_MONGODB_URI['nodelist'])
    MONGODB_DBNAME = _PARSED_MONGODB_URI['database']

    @classmethod
    def to_json(cls):
        return json.dumps({key: cls.__dict__[key] for key in cls.__dict__.keys() if
                           not key.startswith('_') and _is_jsonable(cls.__dict__[key])}, indent=4)


CONNECT_TIMEOUT_S = 5
READ_TIMEOUT_S = 30
AVAILABLE_SERVICE_TIMEOUT_S = 8
TRAINING_TIMEOUT_S = 30

BBOX_ALLOWED_PX_DIFFERENCE = 20
EMB_SIMILARITY_THRESHOLD = 0.01
ALL_SCANNERS = 'Facenet2018', 'InsightFace'
