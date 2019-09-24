from sklearn.linear_model import LogisticRegression

from facerecognition.storage_factory import get_storage
import numpy as np
from threading import Thread

models = {}


def train_async(api_key):
    thread = Thread(target=train, daemon=False, args=[api_key])
    thread.start()


def initial_train():
    api_keys = get_storage().get_api_keys()
    if not api_keys:
        return
    for api_key in api_keys:
        train(api_key)


def train(api_key):
    print('Reading training data from mongo')
    values, labels, face_names = get_storage().get_train_data(api_key)
    if len(face_names) <= 1:
        print("Not enough training data, model hasn't been created")
        return
    print('Training classifier, api key: %s' % api_key)
    model_temp = LogisticRegression(C=100000, solver='lbfgs', multi_class='multinomial')
    model_temp.fit(values, labels)
    print('Training finished, api key: %s' % api_key)
    models[api_key] = {
        "model": model_temp,
        "face_names": face_names
    }


def classify(embedding, api_key):
    if api_key not in models:
        raise RuntimeError("There is no model for api key %s." % api_key)
    model_data = models[api_key]
    predictions = model_data["model"].predict_proba([embedding])[0]
    print("predictions:")
    # print(predictions)
    best_class_indices = np.argsort(-predictions)
    best_class_probability = predictions[best_class_indices[0]]
    print('Best guess: %s with probability %.5f' % (
        model_data["face_names"][best_class_indices[0]], best_class_probability))
    print('Second guess: %s with probability %.5f' % (
        model_data["face_names"][best_class_indices[1]], predictions[best_class_indices[1]]))
    return {
        "prediction": model_data["face_names"][best_class_indices[0]],
        "probability": best_class_probability
    }


def get_face_name(api_key):
    print('Retrieving the data from the database')
    listOfFaces = get_storage().get_all_face_name(api_key)
    if len(listOfFaces)==0:
        print('No faces found in the database for this api-key')
    return listOfFaces

def delete_record(api_key, face_name):
    print('Looking for the record in the database and deleting it')
    get_storage().delete(api_key, face_name)
    print('Records were successfully deleted')