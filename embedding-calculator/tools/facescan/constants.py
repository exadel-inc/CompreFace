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

from src.constants import ENV_MAIN
from src.services.facescan.scanner.facescanner import MockScanner
from src.services.facescan.scanner.facescanners import TESTED_SCANNERS, id_2_face_scanner_cls
from src.services.utils.pyutils import Constants, get_env_split, get_env_bool


class ENV_BENCHMARK(Constants):
    SCANNERS = get_env_split('SCANNERS', ' '.join(s.ID for s in TESTED_SCANNERS))
    LOGGING_LEVEL_NAME = ENV_MAIN.LOGGING_LEVEL_NAME
    DRY_RUN = get_env_bool('DRY_RUN')


def get_scanner(scanner_name):
    if ENV_BENCHMARK.DRY_RUN:
        MockScanner.ID = scanner_name
        return MockScanner()
    return id_2_face_scanner_cls[scanner_name]()
