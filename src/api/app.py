import logging
from http import HTTPStatus
from typing import List

import imageio
from flasgger import swag_from, Swagger
from flask import Flask, request, jsonify, Response

from src.api._decorators import needs_authentication, needs_attached_file, needs_retrain
from src.api.constants import API_KEY_HEADER
from src.api.exceptions import BadRequestException
from src.api.flasgger import template
from src.dto.cropped_face import CroppedFace
from src.face_database.storage_factory import get_storage
from src.face_recognition.embedding_calculator.embedding_calculator import calc_embedding
from src.face_recognition.embedding_classifier.classifier import classify_many, train_async, train_all_models
from src.face_recognition.face_cropper.constants import FaceLimit
from src.face_recognition.face_cropper.crop_face import crop_face, crop_faces

app = Flask(__name__)
swagger = Swagger(app, template=template.template)


@app.route('/status')
@swag_from('flasgger/get_status.yaml')
def status():
    return jsonify(status="OK")


@app.route('/faces')
@swag_from('flasgger/list_faces.yaml')
@needs_authentication
def list_faces():
    api_key = request.headers[API_KEY_HEADER]
    face_names = get_storage().get_all_face_names(api_key)
    return jsonify(names=face_names)


@app.route('/faces/<face_name>', methods=['POST'])
@swag_from('flasgger/add_face_example.yaml')
@needs_authentication
@needs_attached_file
@needs_retrain
def add_face_example(face_name):
    file = request.files['file']
    api_key = request.headers[API_KEY_HEADER]
    img = imageio.imread(file)
    face_img = crop_face(img)
    embedding = calc_embedding(face_img)
    get_storage().save_face(img, face_img, embedding, face_name, api_key)
    return Response(status=HTTPStatus.CREATED)


@app.route('/faces/<face_name>', methods=['DELETE'])
@swag_from('flasgger/remove_face.yaml')
@needs_authentication
@needs_retrain
def remove_face(face_name):
    api_key = request.headers[API_KEY_HEADER]
    get_storage().delete(api_key, face_name)
    return Response(status=HTTPStatus.NO_CONTENT)


@app.route('/retrain', methods=['POST'])
@swag_from('flasgger/retrain_model.yaml')
@needs_authentication
def retrain_model():
    api_key = request.headers[API_KEY_HEADER]
    train_async(api_key)
    return Response(status=HTTPStatus.ACCEPTED)


@app.route('/recognize', methods=['POST'])
@swag_from('flasgger/recognize_faces.yaml')
@needs_authentication
@needs_attached_file
def recognize_faces():
    if 'limit' not in request.values or request.values['limit'] == '':
        limit = FaceLimit.NO_LIMIT
        logging.debug('Limit is not specified, find all faces')
    else:
        limit = int(request.values['limit'])
        logging.debug("the limit is:", limit)
    api_key = request.headers[API_KEY_HEADER]
    file = request.files['file']

    img = imageio.imread(file)
    faces: List[CroppedFace] = crop_faces(img, limit)
    recognized_faces = []
    for face in faces:
        embedding = calc_embedding(face.img)
        recognized_face = classify_many(embedding, api_key, face.box)
        recognized_faces.append(recognized_face)
    logging.debug("The faces that were found:", recognized_faces)

    return jsonify(result=recognized_faces)


@app.errorhandler(BadRequestException)
def handle_api_exception(e: BadRequestException):
    logging.warning(str(e))
    return jsonify(message=e.message), e.http_status


# @app.errorhandler(FaceRecognitionInputError)
# def handle_api_exception(e):
#     logging.warning(str(e))
#     return jsonify(message=str(e)), HTTPStatus.BAD_REQUEST


@app.errorhandler(Exception)
def handle_runtime_error(e):
    logging.critical(str(e))
    return jsonify(message=str(e)), HTTPStatus.INTERNAL_SERVER_ERROR


@app.after_request
def disable_caching(response):
    response.cache_control.max_age = 0
    response.cache_control.no_cache = True
    response.cache_control.no_store = True
    response.cache_control.must_revalidate = True
    response.cache_control.proxy_revalidate = True
    return response


def init_app():
    logging.basicConfig(level=logging.DEBUG)
    train_all_models()
    return app
