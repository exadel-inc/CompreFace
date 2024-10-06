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

from src.services.utils.pyutils import get_env, get_env_split, get_env_bool, Constants

_DEFAULT_SCANNER = 'Facenet2018'


class ENV(Constants):
    ML_PORT = int(get_env('ML_PORT', '3000'))
    IMG_LENGTH_LIMIT = int(get_env('IMG_LENGTH_LIMIT', '640'))

    FACE_DETECTION_PLUGIN = get_env('FACE_DETECTION_PLUGIN', 'facenet.FaceDetector')
    CALCULATION_PLUGIN = get_env('CALCULATION_PLUGIN', 'facenet.Calculator')
    EXTRA_PLUGINS = get_env_split('EXTRA_PLUGINS', 'facenet.LandmarksDetector,agegender.AgeDetector,agegender.GenderDetector,facenet.facemask.MaskDetector,facenet.PoseEstimator')

    LOGGING_LEVEL_NAME = get_env('LOGGING_LEVEL_NAME', 'debug').upper()
    IS_DEV_ENV = get_env('FLASK_ENV', 'production') == 'development'
    BUILD_VERSION = get_env('APP_VERSION_STRING', 'dev')

    GPU_IDX = int(get_env('GPU_IDX', '-1'))
    INTEL_OPTIMIZATION = get_env_bool('INTEL_OPTIMIZATION')

    RUN_MODE = get_env_bool('RUN_MODE', False)


LOGGING_LEVEL = logging._nameToLevel[ENV.LOGGING_LEVEL_NAME]
ENV_MAIN = ENV
SKIPPED_PLUGINS = ["insightface.PoseEstimator", "facemask.MaskDetector", "facenet.PoseEstimator"]
