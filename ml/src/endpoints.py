import os
from http import HTTPStatus

from flask import Response
from flask.json import jsonify
from werkzeug.exceptions import BadRequest

from src.cache import get_storage, get_scanner, get_training_task_manager
from src.constants import ENV
from src.exceptions import NoFaceFoundError
from src.services.async_task_manager.async_task_manager import TaskStatus, TrainingTaskManagerBase
from src.services.classifier.logistic_classifier import LogisticClassifier
from src.services.dto.face_prediction import FacePrediction
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.flask_.constants import API_KEY_HEADER, GetParameter, ARG
from src.services.flask_.needs_api_key import needs_api_key
from src.services.flask_.needs_attached_file import needs_attached_file
from src.services.flask_.needs_retrain import needs_retrain
from src.services.flask_.parse_request_arg import parse_request_bool_arg
from src.services.imgtools.read_img import read_img
from src.services.storage.face import Face
from src.services.storage.mongo_storage import MongoStorage
from src.services.train_classifier import get_faces


def endpoints(app):
    @app.route('/status')
    def status_get():
        return jsonify(status='OK', **_get_hidden_fields())

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

    @app.route('/faces')
    @needs_api_key
    def faces_get():
        from flask import request
        api_key = request.headers[API_KEY_HEADER]
        storage: MongoStorage = get_storage()

        face_names = storage.get_face_names(api_key)

        return jsonify(names=face_names)

    @app.route('/faces/<face_name>', methods=['POST'])
    @needs_api_key
    @needs_attached_file
    @needs_retrain
    def faces_name_post(face_name):
        from flask import request
        img = read_img(request.files['file'])
        api_key = request.headers[API_KEY_HEADER]
        det_prob_threshold = _get_det_prob_threshold(request)
        scanner: FaceScanner = get_scanner()
        storage: MongoStorage = get_storage()

        face = scanner.scan_one(img, det_prob_threshold)
        storage.add_face(api_key,
                         Face(name=face_name, raw_img=img, face_img=face.img, embedding=face.embedding),
                         emb_calc_version=scanner.ID)

        return Response(status=HTTPStatus.CREATED)

    @app.route('/faces/<face_name>', methods=['DELETE'])
    @needs_api_key
    @needs_retrain
    def faces_name_delete(face_name):
        from flask import request
        api_key = request.headers[API_KEY_HEADER]
        storage: MongoStorage = get_storage()

        storage.remove_face(api_key, face_name)

        return Response(status=HTTPStatus.NO_CONTENT)

    @app.route('/retrain', methods=['GET'])
    @needs_api_key
    def retrain_get():
        from flask import request
        api_key = request.headers[API_KEY_HEADER]
        task_manager: TrainingTaskManagerBase = get_training_task_manager()

        training_status = task_manager.get_status(api_key)

        if training_status == TaskStatus.BUSY:
            return Response(status=HTTPStatus.ACCEPTED)
        return jsonify(last_training_status=_get_last_training_status_str(training_status)), HTTPStatus.OK

    @app.route('/retrain', methods=['POST'])
    @needs_api_key
    def retrain_post():
        from flask import request
        api_key = request.headers[API_KEY_HEADER]
        force_start = parse_request_bool_arg(name=GetParameter.FORCE, default=False, request=request)
        task_manager: TrainingTaskManagerBase = get_training_task_manager()

        _check_if_enough_faces_to_train(api_key)
        task_manager.start_training(api_key, force_start)

        return Response(status=HTTPStatus.ACCEPTED)

    @app.route('/retrain', methods=['DELETE'])
    @needs_api_key
    def retrain_delete():
        from flask import request
        api_key = request.headers[API_KEY_HEADER]
        task_manager: TrainingTaskManagerBase = get_training_task_manager()

        task_manager.abort_training(api_key)

        return Response(status=HTTPStatus.NO_CONTENT)

    @app.route('/recognize', methods=['POST'])
    @needs_api_key
    @needs_attached_file
    def recognize_post():
        from flask import request
        img = read_img(request.files['file'])
        det_prob_threshold = _get_det_prob_threshold(request)
        face_limit = _get_face_limit(request)
        scanner: FaceScanner = get_scanner()
        storage: MongoStorage = get_storage()
        api_key = request.headers[API_KEY_HEADER]
        classifier = storage.get_embedding_classifier(api_key, LogisticClassifier.CURRENT_VERSION, scanner.ID)

        predictions = []
        for face in scanner.scan(img, det_prob_threshold):
            prediction = classifier.predict(face.embedding, scanner.ID)
            face_prediction = FacePrediction(prediction.face_name, prediction.probability, face.box)
            predictions.append(face_prediction)

        return jsonify(result=_at_least_one_face(predictions[:face_limit]))


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


def _check_if_enough_faces_to_train(api_key):
    """Raises an error if there's not"""
    get_faces(get_storage(), api_key, get_scanner().ID)


def _at_least_one_face(result):
    if len(result) == 0:
        raise NoFaceFoundError
    return result


def _get_last_training_status_str(training_status):
    return {TaskStatus.IDLE_LAST_NONE: 'NONE',
            TaskStatus.IDLE_LAST_OK: 'OK',
            TaskStatus.IDLE_LAST_ERROR: 'ERROR'}[training_status]


def _get_hidden_fields():
    jenkins_build = os.getenv('APP_VERSION_STRING', '')
    jenkins_be_build = os.getenv('BE_VERSION', '')
    values = {
        '_FORCE_FAIL_E2E_TESTS': ENV.FORCE_FAIL_E2E_TESTS,
        '_JENKINS_BUILD': jenkins_build
    }
    if jenkins_build != jenkins_be_build:
        values['_JENKINS_BE_BUILD'] = jenkins_be_build
    return values
