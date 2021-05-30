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

import requests

from sample_images import IMG_DIR
from sample_images.annotations import name_2_annotation, SAMPLE_IMAGES
from src.constants import ENV_MAIN, LOGGING_LEVEL
from src.exceptions import NoFaceFoundError
from src.init_runtime import init_runtime
from src.services.dto.plugin_result import FaceDTO
from src.services.facescan.scanner.facescanners import scanner
from src.services.facescan.scanner.test.calculate_errors import calculate_errors
from src.services.imgtools.read_img import read_img
from src.services.utils.pyutils import get_env, Constants, get_env_split, get_env_bool, s
from tools._save_img import save_img

logger = logging.getLogger(__name__)


class ENV(Constants):
    USE_REMOTE = get_env_bool('USE_REMOTE')
    ML_HOST = get_env('ML_HOST', 'localhost')
    ML_PORT = ENV_MAIN.ML_PORT
    ML_URL = get_env('ML_URL', f'http://{ML_HOST}:{ML_PORT}')
    if get_env('IMG_NAMES', '') == '':
        IMG_NAMES = [i.img_name for i in SAMPLE_IMAGES]
    elif get_env('IMG_NAMES', '') == '{test_images}':
        IMG_NAMES = [i.img_name for i in SAMPLE_IMAGES if i.include_to_tests]
    else:
        IMG_NAMES = get_env_split('IMG_NAMES')
    SAVE_IMG_str = get_env('SAVE_IMG', 'true').lower()

    LOGGING_LEVEL_NAME = ENV_MAIN.LOGGING_LEVEL_NAME


SAVE_IMG = Constants.str_to_bool(ENV.SAVE_IMG_str)
SAVE_IMG_ON_ERROR = ENV.SAVE_IMG_str == 'on_error'


def _scan_faces_remote(ml_url: str, img_name: str):
    files = {'file': open(IMG_DIR / img_name, 'rb')}
    res = requests.post(f"{ml_url}/scan_faces", files=files)
    if res.status_code == 400 and NoFaceFoundError.description in res.json()['message']:
        return []
    assert res.status_code == 200, res.content
    return [FaceDTO.from_request(r) for r in res.json()['result']]


def _scan_faces_local(img_name):
    img = read_img(IMG_DIR / img_name)
    return scanner.scan(img)


def _scan_faces(img_name: str):
    if ENV.USE_REMOTE:
        return _scan_faces_remote(ENV.ML_URL, img_name)
    else:
        return _scan_faces_local(img_name)


def _calculate_errors(boxes, noses, img_name):
    error_count = 0
    if noses is not None:
        error_count = calculate_errors(boxes, noses)
        if error_count:
            logger.error(f"Found {error_count} error{s(error_count)} in '{img_name}'")
        else:
            logger.info(f"Found all {len(noses)} face{s(len(noses))} in correct places for '{img_name}'")
    else:
        logging.warning(f"Image '{img_name}' is not annotated, skipping")
    return error_count


if __name__ == '__main__':
    init_runtime(logging_level=LOGGING_LEVEL)
    logger.info(ENV.to_json() if ENV_MAIN.IS_DEV_ENV else ENV.to_str())

    total_error_count = 0
    for img_name in ENV.IMG_NAMES:
        boxes = [face.box for face in _scan_faces(img_name)]
        noses = name_2_annotation.get(img_name)

        error_count = _calculate_errors(boxes, noses, img_name)
        total_error_count += error_count

        if SAVE_IMG or SAVE_IMG_ON_ERROR and error_count:
            img = read_img(IMG_DIR / img_name)
            save_img(img, boxes, noses, img_name)

    if total_error_count:
        logger.error(f"Found a total of {total_error_count} error{s(total_error_count)}")
        exit(1)
