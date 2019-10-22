import logging
from http import HTTPStatus

import imageio
from flasgger import swag_from, Swagger
from flask import Flask, request, jsonify, Response
from flask.json import JSONEncoder

from src.api._decorators import needs_authentication, needs_attached_file, needs_retrain
from src.api.constants import API_KEY_HEADER
from src.api.exceptions import BadRequestException
from src.api.flasgger import template
from src.dto.serializable import Serializable
from src.face_recognition.embedding_calculator.calculator import calculate_embedding
from src.face_recognition.embedding_classifier.classifier import train_all_models, train_async, get_face_predictions
from src.face_recognition.face_cropper.constants import FaceLimitConstant
from src.face_recognition.face_cropper.cropper import crop_face
from src.storage.storage_factory import get_storage

app = Flask(__name__)
swagger = Swagger(app, template=template.template)


class MyJSONEncoder(JSONEncoder):
    def default(self, obj):
        if isinstance(obj, Serializable):
            return obj.serialize()
        return super().default(obj)


app.json_encoder = MyJSONEncoder


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
@swag_from('flasgger/add_face.yaml')
@needs_authentication
@needs_attached_file
@needs_retrain
def add_face(face_name):
    file = request.files['file']
    api_key = request.headers[API_KEY_HEADER]

    img = imageio.imread(file)
    face_img = crop_face(img)
    embedding = calculate_embedding(face_img)
    get_storage().add_face(raw_img=img, face_img=face_img, embedding=embedding, face_name=face_name, api_key=api_key)

    return Response(status=HTTPStatus.CREATED)


@app.route('/faces/<face_name>', methods=['DELETE'])
@swag_from('flasgger/remove_face.yaml')
@needs_authentication
@needs_retrain
def remove_face(face_name):
    api_key = request.headers[API_KEY_HEADER]
    get_storage().remove_face(api_key, face_name)
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
    try:
        limit = int(request.values.get('limit', FaceLimitConstant.NO_LIMIT))
        assert limit >= 0
    except ValueError as e:
        raise BadRequestException('Limit format is invalid') from e
    except AssertionError as e:
        raise BadRequestException('Limit value is not invalid') from e
    api_key = request.headers[API_KEY_HEADER]
    file = request.files['file']

    img = imageio.imread(file)
    face_predictions = get_face_predictions(img, limit, api_key)

    return jsonify(result=face_predictions)


@app.errorhandler(BadRequestException)
def handle_api_exception(e: BadRequestException):
    logging.warning(str(e))
    return jsonify(message=e.message), e.http_status


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
