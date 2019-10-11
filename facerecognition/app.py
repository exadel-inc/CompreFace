import functools
import logging
from http import HTTPStatus

import imageio
from flasgger import Swagger, swag_from
from flask import Flask, request, jsonify, Response

from facerecognition import image_helper, tf_helper, storage_factory, classifier
from facerecognition.exceptions import APIKeyNotSpecifiedError, FaceRecognitionAPIException, NoFileSelectedError, \
    NoFileAttachedError
from facerecognition.flasgger import template

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


@app.route('/faces/<face_name>', methods=['POST'])
@swag_from('flasgger/upload_face.yaml')
@needs_authentication
@needs_attached_file
def upload_face(face_name):
    """
    Save image
    """
    file = request.files['file']
    api_key = request.headers[API_KEY_HEADER]
    need_retrain = request.args.get(RETRAIN_PARAM, 'true')
    img = imageio.imread(file)
    face_img = image_helper.crop_faces(img, 1)[0]
    embedding = tf_helper.calc_embedding(face_img)
    storage_factory.get_storage().save_face(img, face_img, embedding, face_name, api_key)
    if need_retrain.lower() == 'true' or need_retrain == '1':
        classifier.train_async(api_key)
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

    img = imageio.imread(file)
    face_img = image_helper.crop_faces(img, limit)
    list_faces = []
    for face in range(0, len(face_img), 2):
        embedding = tf_helper.calc_embedding(face_img[face])
        face = classifier.classify_many(embedding, api_key, face_img[face + 1].tolist())
        if face not in list_faces:
            list_faces.append(face)
    logging.debug("The faces that were found:", list_faces)
    return jsonify(list_faces)


@app.route('/retrain', methods=['POST'])
@swag_from('flasgger/retrain.yaml')
@needs_authentication
def retrain():
    api_key = request.headers[API_KEY_HEADER]
    classifier.train_async(api_key)
    return Response(status=HTTPStatus.CREATED)


@app.route('/faces')
@swag_from('flasgger/get_all_names.yaml')
@needs_authentication
def retrieve_faces():
    api_key = request.headers[API_KEY_HEADER]
    return jsonify(classifier.get_face_name(api_key))


@app.route('/faces/<face_name>', methods=['DELETE'])
@swag_from('flasgger/delete_record.yaml')
@needs_authentication
def delete_record(face_name):
    api_key = request.headers[API_KEY_HEADER]
    classifier.delete_record(api_key, face_name)
    return Response(status=HTTPStatus.NO_CONTENT)


@app.route('/status')
def status():
    return jsonify(status="OK")


@app.errorhandler(RuntimeError)
def handle_runtime_error(e):
    logging.critical(str(e))
    return jsonify(message="Internal Server Error"), HTTPStatus.INTERNAL_SERVER_ERROR


@app.errorhandler(FaceRecognitionAPIException)
def handle_api_exception(e):
    logging.error(str(e))
    return jsonify(message=e.message), e.http_status


@app.after_request
def do_after_request(response):
    response.cache_control.max_age = 0
    response.cache_control.no_cache = True
    response.cache_control.no_store = True
    response.cache_control.must_revalidate = True
    response.cache_control.proxy_revalidate = True
    return response


if __name__ == '__main__':
    app.config.from_mapping(
        SECRET_KEY='dev'
    )
    tf_helper.init()
    classifier.initial_train()
    app.run(debug=True, use_debugger=False, use_reloader=False)
