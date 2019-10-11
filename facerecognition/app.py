import imageio
from flask import Flask, request, jsonify
from flasgger import Swagger, swag_from
from http import HTTPStatus
import logging
from functools import wraps
from .exeptions import APIKeyNotSpecifiedError

from facerecognition import image_helper, tf_helper, storage_factory, classifier
from facerecognition.flasgger import template

app = Flask(__name__)
swagger = Swagger(app, template=template.template)

api_key_header = 'X-Api-Key'
retrain_param = 'retrain'
logging.basicConfig(level=logging.DEBUG)


@app.after_request
def do_after_request(response):
    response.cache_control.max_age = 0
    response.cache_control.no_cache = True
    response.cache_control.no_store = True
    response.cache_control.must_revalidate = True
    response.cache_control.proxy_revalidate = True
    return response


@app.errorhandler(RuntimeError)
def hangleRunTime(e):
    logging.error(str(e))
    return HTTPStatus.INTERNAL_SERVER_ERROR


@app.errorhandler(APIKeyNotSpecifiedError)
def handleAPIKeyError(e):
    logging.error(str(e))
    return jsonify(HTTPStatus.FORBIDDEN)


def needs_authentication(f):
    @wraps(f)
    def wrapper(*args, **kwargs):
        if api_key_header not in request.headers:
            raise APIKeyNotSpecifiedError
        return f(*args, **kwargs)

    return wrapper


@app.route('/faces/<face_name>', methods=['POST'])
@swag_from('flasgger/upload_face.yaml')
@needs_authentication
def upload_face(face_name):
    """
    Save image
    """
    if 'file' not in request.files:
        logging.error('No file part')
        return jsonify(HTTPStatus.BAD_REQUEST)
    file = request.files['file']
    if file.filename == '':
        logging.error('No selected file')
        return jsonify(HTTPStatus.BAD_REQUEST)
    api_key = request.headers[api_key_header]
    need_retrain = request.args.get(retrain_param, 'true')
    img = imageio.imread(file)
    face_img = image_helper.crop_faces(img, 1)[0]
    embedding = tf_helper.calc_embedding(face_img)
    storage_factory.get_storage().save_face(img, face_img, embedding, face_name, api_key)
    if need_retrain == 'true' or need_retrain == 'True' or need_retrain == '1':
        classifier.train_async(api_key)
    return jsonify(HTTPStatus.CREATED)


@app.route('/recognize', methods=['POST'])
@swag_from('flasgger/recognize_faces.yaml')
@needs_authentication
def recognize_faces():
    if 'file' not in request.files:
        logging.error('No file part')
        return jsonify(HTTPStatus.BAD_REQUEST)
    if 'limit' not in request.values or request.values['limit'] == '':
        limit = -1
        logging.debug('Limit is not specified, find all faces')
    else:
        limit = int(request.values['limit'])
        logging.debug("the limit is:", limit)
    api_key = request.headers[api_key_header]
    file = request.files['file']
    if file.filename == '':
        logging.error('No selected file')
        return jsonify(HTTPStatus.BAD_REQUEST)

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
    api_key = request.headers[api_key_header]
    classifier.train_async(api_key)
    return jsonify(HTTPStatus.CREATED)


@app.route('/faces')
@swag_from('flasgger/get_all_names.yaml')
@needs_authentication
def retrieve_faces():
    api_key = request.headers[api_key_header]
    return jsonify(classifier.get_face_name(api_key))


@app.route('/faces/<face_name>', methods=['DELETE'])
@swag_from('flasgger/delete_record.yaml')
@needs_authentication
def delete_record(face_name):
    api_key = request.headers[api_key_header]
    classifier.delete_record(api_key, face_name)
    return jsonify(HTTPStatus.NO_CONTENT)


logging.info("__name__ %s" % __name__)
if __name__ == 'facerecognition.app':
    app.config.from_mapping(
        SECRET_KEY='dev'
    )
    tf_helper.init()
    classifier.initial_train()
    app.run(debug=True, use_debugger=False, use_reloader=False)
