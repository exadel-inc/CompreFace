from flask.json import jsonify
from werkzeug.exceptions import BadRequest

from src.cache import get_scanner
from src.services.flaskext.constants import ARG
from src.services.flaskext.file_attachments import needs_attached_file
from src.services.utils.nputils import read_img


def endpoints(app):
    @app.route('/status')
    def get_status():
        return jsonify(status="OK")

    @app.route('/scan_faces', methods=['POST'])
    @needs_attached_file
    def scan_faces_post():
        from flask import request
        img = read_img(request.files['file'])
        face_limit = _get_face_limit(request)
        detection_threshold_c = _get_detection_threshold_c(request)

        scanned_faces = get_scanner().scan(img, face_limit, detection_threshold_c)

        return jsonify(calculator_version=get_scanner().ID, result=scanned_faces)


def _get_detection_threshold_c(request):
    detection_threshold_c = request.values.get(ARG.DET_PROB_THRESHOLD)
    return float(detection_threshold_c) if detection_threshold_c is not None else None


def _get_face_limit(request):
    limit = request.values.get(ARG.LIMIT)
    if limit is None:
        return limit

    try:
        limit = int(limit)
    except ValueError as e:
        raise BadRequest('Limit format is invalid (limit >= 0)') from e

    if not (limit >= 0):
        raise BadRequest('Limit value is invalid (limit >= 0)')

    return limit
