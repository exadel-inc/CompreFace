import imageio
from flask import Flask, flash, request, redirect, jsonify
from flasgger import Swagger, swag_from

from facerecognition import image_helper, tf_helper, storage_factory, classifier
from facerecognition.flasgger import template

app = Flask(__name__)
swagger = Swagger(app, template=template.template)

api_key_header = 'X-Api-Key'
retrain_param = 'retrain'


@app.route('/save/<face_name>', methods=['POST'])
@swag_from('flasgger/upload_face.yaml')
def upload_face(face_name):
    """
    Save image
    """
    if 'file' not in request.files:
        print('No file part')
        return 'No file part', 400
    file = request.files['file']
    if file.filename == '':
        print('No selected file')
        return 'No selected file', 400
    if api_key_header not in request.headers:
        print('Api key not specified')
        return 'Api key not specified', 400
    api_key = request.headers[api_key_header]
    need_retrain = request.args.get(retrain_param, 'true')
    try:
        img = imageio.imread(file)
        face_img = image_helper.crop_faces(img)[0]
        embedding = tf_helper.calc_embedding(face_img)
        storage_factory.get_storage().save_face(img, face_img, embedding, face_name, api_key)
        if need_retrain == 'true':
            classifier.train_async(api_key)
    except RuntimeError as e:
        print(str(e))
        return str(e), 400
    return '', 201


@app.route('/recognize', methods=['POST'])
@swag_from('flasgger/recognize_faces.yaml')
def recognize_faces():

    if 'file' not in request.files:
        flash('No file part')
        return redirect(request.url), 400
    if api_key_header not in request.headers:
        print('Api key not specified')
        return 'Api key not specified', 400
    if 'limit' not in request.values or request.values['limit']=='':
        limit = -1
        print('Limit is not specified, find all faces')
    else:
        limit = int(request.values['limit'])
        print("the limit is:", limit)
    api_key = request.headers[api_key_header]
    file = request.files['file']
    if file.filename == '':
        print('No selected file')
        return 'No selected file', 400
    try:
        img = imageio.imread(file)
        face_img = image_helper.crop_faces(img, limit)
        list_faces = []
        for face in range(0, len(face_img), 2):
            embedding = tf_helper.calc_embedding(face_img[face])
            face = classifier.classifyMany(embedding, api_key, face_img[face + 1].tolist())
            if face not in list_faces:
                list_faces.append(face)
        print("The faces that were found:", list_faces)
        return jsonify(list_faces)
    except RuntimeError as e:
        print(str(e))
        return str(e), 400


@app.route('/retrain', methods=['POST'])
@swag_from('flasgger/retrain.yaml')
def retrain():
    if api_key_header not in request.headers:
        print('Api key not specified')
        return 'Api key not specified', 400
    api_key = request.headers[api_key_header]
    try:
        classifier.train_async(api_key)
    except RuntimeError as e:
        print(str(e))
        return str(e), 400
    return '', 201

@app.route('/all')
@swag_from('flasgger/get_all_names.yaml')
def retrieveFaces():
    if api_key_header not in request.headers:
        print('Api key not specified')
        return 'Api key not specified', 400
    try:
        api_key = request.headers[api_key_header]
        return jsonify(classifier.get_face_name(api_key)), 200

    except RuntimeError as e:
        print(str(e))
        return str(e), 400


@app.route('/remove/<face_name>', methods=['DELETE'])
@swag_from('flasgger/delete_record.yaml')
def deleteRecord(face_name):
    if api_key_header not in request.headers:
        print('Api key not specified')
        return 'Api key not specified', 400
    try:
        api_key = request.headers[api_key_header]
        classifier.delete_record(api_key, face_name)
    except RuntimeError as e:
        print(str(e))
        return str(e), 400
    return '', 204


@app.route('/status', methods=['GET'])
def status():
    return jsonify({'status': 'OK'}), 200


print("__name__ %s" % __name__)
if __name__ == '__main__':
    app.config.from_mapping(
        SECRET_KEY='dev'
    )
    tf_helper.init()
    classifier.initial_train()
    app.run(debug=True, use_debugger=False, use_reloader=False)
