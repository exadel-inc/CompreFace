import logging

import requests

from sample_images import IMG_DIR
from sample_images.annotations import name_2_annotation, SAMPLE_IMAGES
from src.constants import ENV
from src.exceptions import NoFaceFoundError
from src.logging_ import init_runtime
from src.services.dto.scanned_face import ScannedFace
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.facescan.scanner.facescanners import id_2_face_scanner_cls
from src.services.facescan.scanner.test.calculate_errors import calculate_errors
from src.services.imgtools.read_img import read_img
from src.services.utils.pyutils import get_env, Constants


class _ENV(Constants):
    USE_REMOTE = get_env('USE_REMOTE', 'false').lower() in ('true', '1')
    ML_HOST = get_env('ML_HOST', 'localhost')
    ML_PORT = int(get_env('ML_PORT', '3000'))
    ML_URL = get_env('ML_URL', f'http://{ML_HOST}:{ML_PORT}')
    IMG_NAMES = Constants.split(get_env('IMG_NAMES', ' '.join([i.image_name for i in SAMPLE_IMAGES])))
    SHOW_IMG = get_env('SHOW_IMG', 'true').lower() in ('true', '1')
    SHOW_IMG_ON_ERROR = get_env('SHOW_IMG_ON_ERROR', 'false').lower() in ('true', '1')


def _scan_faces_remote(ml_url, img_name):
    files = {'file': open(IMG_DIR / img_name, 'rb')}
    res = requests.post(f"{ml_url}/scan_faces", files=files)
    if res.status_code == 400 and res.json()['message'] == NoFaceFoundError.description:
        return []
    assert res.status_code == 200, res.content
    return [ScannedFace.from_request(r) for r in res.json()['result']]


def _scan_faces_local(scanner_id, img_name):
    img = read_img(IMG_DIR / img_name)
    scanner: FaceScanner = id_2_face_scanner_cls[scanner_id]()
    return scanner.scan(img)


def _scan_faces(img_name):
    if _ENV.USE_REMOTE:
        return _scan_faces_remote(_ENV.ML_URL, img_name)
    else:
        return _scan_faces_local(ENV.SCANNER, img_name)


def _calculate_errors(scanned_faces, img_name):
    if img_name not in name_2_annotation:
        logging.warning(f"Image '{img_name}' is not annotated, skipping")
        return 0

    boxes = [face.box for face in scanned_faces]
    noses = name_2_annotation[img_name]
    errors = calculate_errors(boxes, noses)

    if errors:
        logging.warning(f"Found {errors} error(s) in '{img_name}'")
    return errors


if __name__ == '__main__':
    init_runtime(logging_level=logging.INFO)
    logging.debug(_ENV.__str__())

    total_errors = 0
    for img_name in _ENV.IMG_NAMES:
        scanned_faces = _scan_faces(img_name)
        errors = _calculate_errors(scanned_faces, img_name)

        if _ENV.SHOW_IMG or _ENV.SHOW_IMG_ON_ERROR and errors:
            ScannedFace.show(scanned_faces)

        total_errors += errors
    logging.info(f"Found {f'a total of {total_errors}' if total_errors else 'no'} error(s)")
