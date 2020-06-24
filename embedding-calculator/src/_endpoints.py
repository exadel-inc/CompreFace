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

import io
import sys
from contextlib import redirect_stdout
from unittest.mock import patch

from flask.json import jsonify
from werkzeug.exceptions import BadRequest
from yolk.cli import Yolk

from src.cache import get_scanner
from src.constants import ENV
from src.exceptions import NoFaceFoundError
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.flask_.constants import ARG
from src.services.flask_.needs_attached_file import needs_attached_file
from src.services.imgtools.read_img import read_img


def endpoints(app):
    # TODO EFRS-497 Temporary endpoint for development (remove once the task is done)
    @app.route('/licenses')
    def licenses_get():
        with io.StringIO() as output_buffer, redirect_stdout(output_buffer):
            with patch.object(sys, 'argv', ['', '-l', '-f', 'license,home-page']):
                Yolk().run()
            return output_buffer.getvalue().replace('\n', '<br>')

    @app.route('/status')
    def status_get():
        return jsonify(status='OK', build_version=ENV.BUILD_VERSION, calculator_version=ENV.SCANNER)

    @app.route('/scan_faces', methods=['POST'])
    @needs_attached_file
    def scan_faces_post():
        from flask import request
        img = read_img(request.files['file'])
        face_limit = _get_face_limit(request)
        scanner: FaceScanner = get_scanner()
        det_prob_threshold = _get_det_prob_threshold(request)

        scanned_faces = scanner.scan(img, det_prob_threshold)

        return jsonify(calculator_version=get_scanner().ID, result=_at_least_one_face(scanned_faces[:face_limit]))


def _get_det_prob_threshold(request):
    det_prob_threshold_val = request.values.get(ARG.DET_PROB_THRESHOLD)
    if det_prob_threshold_val is None:
        return None
    det_prob_threshold = float(det_prob_threshold_val)
    if not (0 <= det_prob_threshold <= 1):
        raise BadRequest('Detection threshold incorrect (0 <= det_prob_threshold <= 1)')
    return det_prob_threshold


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


def _at_least_one_face(result):
    if len(result) == 0:
        raise NoFaceFoundError
    return result
