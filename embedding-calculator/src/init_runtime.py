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
import os
import sys

from PIL import ImageFile

from src._logging import init_logging
from src.constants import ENV


def _check_ci_build_args():
    app_version_string = os.getenv('APP_VERSION_STRING', '')
    be_version = os.getenv('BE_VERSION', '')
    if app_version_string != be_version:
        logging.warning(f"APP_VERSION_STRING='{app_version_string}' "
                        f"and BE_VERSION='{be_version}' have different values")


def init_runtime(logging_level):
    assert sys.version_info >= (3, 7)
    ImageFile.LOAD_TRUNCATED_IMAGES = True
    _check_ci_build_args()
    ENV.RUN_MODE = True
    init_logging(logging_level)