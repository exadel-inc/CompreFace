from flask import Flask, jsonify

from src.shared.api.endpoints_decorators.needs_attached_file import needs_attached_file
from src.shared.api.exceptions import BadRequestException
from src.shared.facescan.read_img import read_img
from src.shared.facescan.types import ReturnLimitConstant
from src.singletons.scanner import get_scanner


def endpoints(app: Flask):
    @app.route('/scan_faces', methods=['POST'])
    @needs_attached_file
    def scan_faces_post():
        from flask import request
        img = read_img(request.files['file'])
        face_limit = _get_face_limit(request)
        detection_threshold_c = _get_detection_threshold_c(request)

        scanned_faces = get_scanner().scan(img, face_limit, detection_threshold_c)

        return jsonify(calculator_version=get_scanner().ID, result=scanned_faces)


def _get_face_limit(request):
    try:
        face_limit = int(request.form.get('limit', ReturnLimitConstant.NO_LIMIT))
        assert face_limit >= 0
    except ValueError as e:
        raise BadRequestException('Limit format is invalid') from e
    except AssertionError as e:
        raise BadRequestException('Limit value is invalid') from e
    return face_limit


def _get_detection_threshold_c(request):
    threshold_c = request.form.get('threshold_c')

    if threshold_c is None:
        return None

    threshold_c_num = float(threshold_c)
    if not (0 <= threshold_c_num <= 1):
        raise BadRequestException('Threshold value is invalid')

    return threshold_c_num
