#  Copyright (c) 2020 the original author or authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

import logging

from pymongo import uri_parser

from src.services.utils.pyutils import get_env, first_and_only, Constants, get_env_bool


class ENV(Constants):
    ML_PORT = int(get_env('ML_PORT', '3000'))
    SCANNER = get_env('SCANNER', 'Facenet2018')
    IMG_LENGTH_LIMIT = int(get_env('IMG_LENGTH_LIMIT', '640'))

    _MONGODB_HOST = get_env('MONGODB_HOST', 'mongodb')
    _MONGODB_PORT = int(get_env('MONGODB_PORT', '27017'))
    _MONGODB_DBNAME = get_env('MONGODB_DBNAME', 'efrs_db')
    MONGODB_URI = get_env('MONGODB_URI', f'mongodb://{_MONGODB_HOST}:{_MONGODB_PORT}/{_MONGODB_DBNAME}')
    _MONGODB_PARSED_URI = uri_parser.parse_uri(MONGODB_URI)
    MONGODB_HOST, MONGODB_PORT = first_and_only(_MONGODB_PARSED_URI['nodelist'])
    MONGODB_DBNAME = _MONGODB_PARSED_URI['database']

    LOGGING_LEVEL_NAME = get_env('LOGGING_LEVEL_NAME', 'debug').upper()
    IS_DEV_ENV = get_env('FLASK_ENV', 'production') == 'development'
    FORCE_FAIL_E2E_TESTS = get_env_bool('FORCE_FAIL_E2E_TESTS')
    BUILD_VERSION = get_env('APP_VERSION_STRING', 'dev')


LOGGING_LEVEL = logging._nameToLevel[ENV.LOGGING_LEVEL_NAME]
ENV_MAIN = ENV
