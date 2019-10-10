import imageio
from flask import Flask, flash, request, redirect, jsonify, make_response
from flasgger import Swagger, swag_from
from http import HTTPStatus
import logging

from facerecognition import image_helper, tf_helper, storage_factory, classifier
from facerecognition.flasgger import template

app = Flask(__name__)
swagger = Swagger(app, template=template.template)

api_key_header = 'X-Api-Key'
retrain_param = 'retrain'
logging.basicConfig(level=logging.DEBUG)

@app.route('/faces/<face_name>', methods=['POST'])
@swag_from('flasgger/upload_face.yaml')
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
    if api_key_header not in request.headers:
        logging.error('Api key not specified')
        return jsonify(HTTPStatus.FORBIDDEN)
    api_key = request.headers[api_key_header]
    need_retrain = request.args.get(retrain_param, 'true')
    try:
        img = imageio.imread(file)
        face_img = image_helper.crop_faces(img, 1)[0]
        embedding = tf_helper.calc_embedding(face_img)
        storage_factory.get_storage().save_face(img, face_img, embedding, face_name, api_key)
        if need_retrain == 'true' or need_retrain == 'True' or need_retrain == '1':
            classifier.train_async(api_key)
    except RuntimeError as e:
        logging.error(str(e))
        return jsonify(HTTPStatus.INTERNAL_SERVER_ERROR)
    return jsonify(HTTPStatus.CREATED)


@app.route('/recognize', methods=['POST'])
@swag_from('flasgger/recognize_faces.yaml')
def recognize_faces():

    if 'file' not in request.files:
        logging.error('No file part')
        return jsonify(HTTPStatus.BAD_REQUEST)
    if api_key_header not in request.headers:
        logging.error('Api key not specified')
        return jsonify(HTTPStatus.FORBIDDEN)
    if 'limit' not in request.values or request.values['limit']=='':
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
    try:
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
    except RuntimeError as e:
        logging.error(str(e))
        return jsonify(HTTPStatus.INTERNAL_SERVER_ERROR)


@app.route('/retrain', methods=['POST'])
@swag_from('flasgger/retrain.yaml')
def retrain():
    if api_key_header not in request.headers:
        logging.error('Api key not specified')
        return jsonify(HTTPStatus.FORBIDDEN)
    api_key = request.headers[api_key_header]
    try:
        classifier.train_async(api_key)
    except RuntimeError as e:
        logging.error(str(e))
        return jsonify(HTTPStatus.INTERNAL_SERVER_ERROR)
    return jsonify(HTTPStatus.CREATED)

@app.route('/faces')
@swag_from('flasgger/get_all_names.yaml')
def retrieve_faces():
    if api_key_header not in request.headers:
        logging.error('Api key not specified')
        return jsonify(HTTPStatus.FORBIDDEN)
    try:
        api_key = request.headers[api_key_header]
        return jsonify(classifier.get_face_name(api_key))

    except RuntimeError as e:
        logging.error(str(e))
        return jsonify(HTTPStatus.INTERNAL_SERVER_ERROR)



@app.route('/faces/<face_name>', methods=['DELETE'])
@swag_from('flasgger/delete_record.yaml')
def delete_record(face_name):
    if api_key_header not in request.headers:
        logging.error('Api key not specified')
        return jsonify(HTTPStatus.FORBIDDEN)
    try:
        api_key = request.headers[api_key_header]
        classifier.delete_record(api_key, face_name)
    except RuntimeError as e:
        logging.error(str(e))
        return jsonify(HTTPStatus.INTERNAL_SERVER_ERROR)
    return jsonify(HTTPStatus.NO_CONTENT)


logging.info("__name__ %s" % __name__)
if __name__ == 'facerecognition.app':
    app.config.from_mapping(
        SECRET_KEY='dev'
    )
    tf_helper.init()
    classifier.initial_train()
    app.run(debug=True, use_debugger=False, use_reloader=False)
