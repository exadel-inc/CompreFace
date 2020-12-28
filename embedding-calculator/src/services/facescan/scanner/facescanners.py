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
from src.services.facescan.scanner.facenet.facenet import Facenet2018
from src.services.facescan.scanner.facescanner import MockScanner
from src.services.facescan.scanner.insightface.insightface import InsightFace

_ALL_SCANNERS = Facenet2018, InsightFace, MockScanner
id_2_face_scanner_cls = {backend.ID: backend for backend in _ALL_SCANNERS}
TESTED_SCANNERS = [id_2_face_scanner_cls[k] for k in ENV_MAIN.SCANNERS]
