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

from src.services.utils.pyutils import get_env, Constants, get_env_split

_DEFAULT_SCANNERS = 'Facenet2018,InsightFace'


class ENV(Constants):
    ML_PORT = int(get_env('ML_PORT', '3000'))
    SCANNER = get_env_split('SCANNER', _DEFAULT_SCANNERS)[0]
    SCANNERS = get_env_split('SCANNER', _DEFAULT_SCANNERS)
    IMG_LENGTH_LIMIT = int(get_env('IMG_LENGTH_LIMIT', '640'))

    LOGGING_LEVEL_NAME = get_env('LOGGING_LEVEL_NAME', 'debug').upper()
    IS_DEV_ENV = get_env('FLASK_ENV', 'production') == 'development'
    BUILD_VERSION = get_env('APP_VERSION_STRING', 'dev')


LOGGING_LEVEL = logging._nameToLevel[ENV.LOGGING_LEVEL_NAME]
ENV_MAIN = ENV
