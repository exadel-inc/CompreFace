import functools
import logging
from http import HTTPStatus

import imageio
from flasgger import Swagger, swag_from
from flask import Flask, request, jsonify, Response

from facerecognition import core
from facerecognition.api.exceptions import APIKeyNotSpecifiedError, NoFileSelectedError, \
    NoFileAttachedError, BadRequestException
from facerecognition.api.flasgger import template
from facerecognition.core.exceptions import FaceRecognitionInputError
from facerecognition.database import get_storage

app = Flask(__name__)
swagger = Swagger(app, template=template.template)
logging.basicConfig(level=logging.DEBUG)

API_KEY_HEADER = 'X-Api-Key'
RETRAIN_PARAM = 'retrain'


def needs_authentication(f):
    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        if API_KEY_HEADER not in request.headers:
            raise APIKeyNotSpecifiedError

        return f(*args, **kwargs)

    return wrapper


def needs_attached_file(f):
    @functools.wraps(f)
    def wrapper(*args, **kwargs):
        if 'file' not in request.files:
            raise NoFileAttachedError

        file = request.files['file']
        if file.filename == '':
            raise NoFileSelectedError

        return f(*args, **kwargs)

    return wrapper


@app.route('/status')
def status():
    return jsonify(status="OK")


@app.route('/faces/<face_name>', methods=['POST'])
@swag_from('flasgger/add_face.yaml')
@needs_authentication
@needs_attached_file
def upload_face(face_name):
    file = request.files['file']
    api_key = request.headers[API_KEY_HEADER]
    do_retrain = request.args.get(RETRAIN_PARAM, 'true').lower() in ('true', '1')

    img = imageio.imread(file)
    face_img = core.crop_face(img)
    embedding = core.calc_embedding(face_img)
    get_storage().save_face(img, face_img, embedding, face_name, api_key)

    if do_retrain:
        core.train_model(api_key)

    return Response(status=HTTPStatus.CREATED)


@app.route('/recognize', methods=['POST'])
@swag_from('flasgger/recognize_faces.yaml')
@needs_authentication
@needs_attached_file
def recognize_faces():
    if 'limit' not in request.values or request.values['limit'] == '':
        limit = -1
        logging.debug('Limit is not specified, find all faces')
    else:
        limit = int(request.values['limit'])
        logging.debug("the limit is:", limit)
    api_key = request.headers[API_KEY_HEADER]
    file = request.files['file']

    recognition_result = core.recognize_faces(limit, file, api_key)
    logging.debug("The faces that were found:", recognition_result)

    return jsonify(recognition_result)


@app.route('/retrain', methods=['POST'])
@swag_from('flasgger/retrain_model.yaml')
@needs_authentication
def retrain_model():
    api_key = request.headers[API_KEY_HEADER]

    core.train_model(api_key)

    return Response(status=HTTPStatus.CREATED)


@app.route('/faces')
@swag_from('flasgger/list_faces.yaml')
@needs_authentication
def list_faces():
    api_key = request.headers[API_KEY_HEADER]

    logging.debug('Retrieving the data from the database')
    face_names = get_storage().get_all_face_names(api_key)
    if len(face_names) == 0:
        logging.warning('No faces found in the database for this api-key')

    return jsonify(face_names)


@app.route('/faces/<face_name>', methods=['DELETE'])
@swag_from('flasgger/remove_face.yaml')
@needs_authentication
def remove_face(face_name):
    api_key = request.headers[API_KEY_HEADER]

    logging.debug('Looking for the record in the database and deleting it')
    get_storage().delete(api_key, face_name)
    logging.debug('Records were successfully deleted')

    return Response(status=HTTPStatus.NO_CONTENT)


@app.after_request
def do_after_request(response):
    response.cache_control.max_age = 0
    response.cache_control.no_cache = True
    response.cache_control.no_store = True
    response.cache_control.must_revalidate = True
    response.cache_control.proxy_revalidate = True
    return response


@app.errorhandler(BadRequestException)
def handle_api_exception(e: BadRequestException):
    logging.warning(str(e))
    return jsonify(message=e.message), e.http_status


@app.errorhandler(FaceRecognitionInputError)
def handle_api_exception(e):
    logging.warning(str(e))
    return jsonify(message=str(e)), HTTPStatus.BAD_REQUEST


@app.errorhandler(Exception)
def handle_runtime_error(e):
    logging.critical(str(e))
    return jsonify(message=str(e)), HTTPStatus.INTERNAL_SERVER_ERROR


if __name__ == '__main__':
    app.config.from_mapping(SECRET_KEY='dev')
    core.init()
    app.run(debug=True, use_debugger=False, use_reloader=False)
