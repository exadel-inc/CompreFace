import logging

from pymongo import uri_parser

from src.services.utils.pyutils import get_env, first_and_only, Constants


class ENV(Constants):
    ML_PORT = int(get_env('ML_PORT', '3000'))
    SCANNER = get_env('SCANNER', 'InsightFace')
    IMG_LENGTH_LIMIT = int(get_env('IMG_LENGTH_LIMIT', '520'))

    _MONGODB_HOST = get_env('MONGODB_HOST', 'mongodb')
    _MONGODB_PORT = int(get_env('MONGODB_PORT', '27017'))
    _MONGODB_DBNAME = get_env('MONGODB_DBNAME', 'efrs_db')
    MONGODB_URI = get_env('MONGODB_URI', f'mongodb://{_MONGODB_HOST}:{_MONGODB_PORT}/{_MONGODB_DBNAME}')
    _MONGODB_PARSED_URI = uri_parser.parse_uri(MONGODB_URI)
    MONGODB_HOST, MONGODB_PORT = first_and_only(_MONGODB_PARSED_URI['nodelist'])
    MONGODB_DBNAME = _MONGODB_PARSED_URI['database']

    LOGGING_LEVEL_NAME = get_env('LOGGING_LEVEL_NAME', 'debug').upper()
    IS_DEV_ENV = get_env('FLASK_ENV', 'production') == 'development'
    DO_LOG_STACKTRACE = get_env('DO_LOG_STACKTRACE', 'true').lower() in ('true', '1')
    DO_LOG_MULTITASKING_IDS = get_env('DO_LOG_MULTITASKING_IDS', 'false').lower() in ('true', '1')
    FORCE_FAIL_E2E_TESTS = get_env('FORCE_FAIL_E2E_TESTS', 'false').lower() in ('true', '1')


LOGGING_LEVEL = logging._nameToLevel[ENV.LOGGING_LEVEL_NAME]
