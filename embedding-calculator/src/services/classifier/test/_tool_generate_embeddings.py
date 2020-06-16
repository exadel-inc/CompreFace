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

import joblib

from sample_images import IMG_DIR
from src.cache import get_scanner
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.imgtools.read_img import read_img
from src.services.utils.pyutils import get_current_dir

CURRENT_DIR = get_current_dir(__file__)


def generate_embedding_from_img(filename):
    img = read_img(IMG_DIR / f'{filename}.jpg')
    scanner: FaceScanner = get_scanner()
    embedding = scanner.scan_one(img).embedding
    joblib.dump(embedding, CURRENT_DIR / f'{filename}.embedding.joblib')


if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    generate_embedding_from_img('01.A')
    generate_embedding_from_img('02.A')
    generate_embedding_from_img('07.B')
