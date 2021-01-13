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
from typing import List, Optional

from flask import request
from flask.json import jsonify
from werkzeug.exceptions import BadRequest

from src.constants import ENV
from src.exceptions import NoFaceFoundError
from src.services.facescan.plugins import managers
from src.services.facescan.scanner.facescanners import scanner
from src.services.flask_.constants import ARG
from src.services.flask_.needs_attached_file import needs_attached_file
from src.services.imgtools.read_img import read_img
from src.services.utils.pyutils import Constants


def endpoints(app):
    @app.route('/status')
    def status_get():
        available_plugins = {p.slug: str(p)
                             for p in managers.plugin_manager.plugins}
        calculator = managers.plugin_manager.calculator
        return jsonify(status='OK', build_version=ENV.BUILD_VERSION,
                       calculator_version=str(calculator),
                       available_plugins=available_plugins)

    @app.route('/find_faces', methods=['POST'])
    @needs_attached_file
    def find_faces_post():
        detector = managers.plugin_manager.detector
        face_plugins = managers.plugin_manager.filter_face_plugins(
            _get_face_plugin_names()
        )
        faces = detector(
            img=read_img(request.files['file']),
            det_prob_threshold=_get_det_prob_threshold(),
            face_plugins=face_plugins
        )
        plugins_versions = {p.slug: str(p) for p in [detector] + face_plugins}
        faces = _limit(faces, request.values.get(ARG.LIMIT))
        return jsonify(plugins_versions=plugins_versions, result=faces)

    @app.route('/scan_faces', methods=['POST'])
    @needs_attached_file
    def scan_faces_post():
        faces = scanner.scan(
            img=read_img(request.files['file']),
            det_prob_threshold=_get_det_prob_threshold()
        )
        faces = _limit(faces, request.values.get(ARG.LIMIT))
        return jsonify(calculator_version=scanner.ID, result=faces)


def _get_det_prob_threshold():
    det_prob_threshold_val = request.values.get(ARG.DET_PROB_THRESHOLD)
    if det_prob_threshold_val is None:
        return None
    det_prob_threshold = float(det_prob_threshold_val)
    if not (0 <= det_prob_threshold <= 1):
        raise BadRequest('Detection threshold incorrect (0 <= det_prob_threshold <= 1)')
    return det_prob_threshold


def _get_face_plugin_names() -> Optional[List[str]]:
    if ARG.FACE_PLUGINS not in request.values:
        return []
    return [
        name for name in Constants.split(request.values[ARG.FACE_PLUGINS])
    ]


def _limit(faces: List, limit: str = None) -> List:
    """
    >>> _limit([1, 2, 3], None)
    [1, 2, 3]
    >>> _limit([1, 2, 3], '')
    [1, 2, 3]
    >>> _limit([1, 2, 3], 0)
    [1, 2, 3]
    >>> _limit([1, 2, 3], 1)
    [1]
    >>> _limit([1, 2, 3], 2)
    [1, 2]
    """
    if len(faces) == 0:
        raise NoFaceFoundError

    try:
        limit = int(limit or 0)
    except ValueError as e:
        raise BadRequest('Limit format is invalid (limit >= 0)') from e
    if not (limit >= 0):
        raise BadRequest('Limit value is invalid (limit >= 0)')

    return faces[:limit] if limit else faces
