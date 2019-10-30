import logging
from multiprocessing import Process

from sklearn.linear_model import LogisticRegression

from src.dto.trained_model import TrainedModel
from src.storage.storage import get_storage
from src.storage.trained_model_storage import save_trained_model, delete_trained_model

currently_training_api_keys = []

def get_trained_classifier(values, labels):
    classifier = LogisticRegression(C=100000, solver='lbfgs', multi_class='multinomial')
    classifier.fit(values, labels)
    return classifier


def train(api_key):
    values, labels, pred_class_to_face_name = get_storage().get_classifier_training_data(api_key)
    if len(pred_class_to_face_name) <= 1:
        logging.warning("Not enough training data, model hasn't been created")
        delete_trained_model(api_key)
        return

    logging.debug('Training started, api key: %s', api_key)
    classifier = get_trained_classifier(values, labels)
    logging.debug('Training finished, api key: %s', api_key)

    save_trained_model(api_key, TrainedModel(classifier=classifier, class_2_face_name=pred_class_to_face_name))


def train_async(api_key):
    global currently_training_api_keys
    process = Process(target=train, daemon=False, args=[api_key])
    if not process.is_alive() and api_key in currently_training_api_keys:
        logging.debug('the api key is going to be removed')
        currently_training_api_keys.remove(api_key)

    if api_key in currently_training_api_keys:
        logging.debug('we are not retraining')
        return False
    logging.debug('the api keys that are currently training: %s' % currently_training_api_keys)
    process.run()
    if process.is_alive():
        currently_training_api_keys.append(api_key)
    return True


def train_all_models():
    api_keys = get_storage().get_api_keys()
    if not api_keys:
        logging.warning("Face classifier training for all models hasn't been started, "
                        "because no API Keys were found in storage.")
        return

    for api_key in api_keys:
        train(api_key)
