import logging
from multiprocessing import Process

from sklearn.linear_model import LogisticRegression

from src.dto.trained_model import TrainedModel
from src.storage.storage import get_storage
from src.storage.trained_model_storage import save_trained_model, delete_trained_model
from src.face_recognition.embedding_classifier.exceptions import ClassifierIsAlreadyTrainingError

currently_training_api_keys = {}

def cancel_training(api_key):
    logging.debug("currently in the training list %s" %currently_training_api_keys)
    if api_key in currently_training_api_keys and currently_training_api_keys[api_key].is_alive() == False:
        del currently_training_api_keys[api_key]
    logging.debug("currently in the training list %s" %currently_training_api_keys)
    if api_key in currently_training_api_keys:
        logging.debug("currently in the training list %s" % currently_training_api_keys[api_key])
        currently_training_api_keys[api_key].terminate()
        del currently_training_api_keys[api_key]
    logging.debug("currently in the training list %s" %currently_training_api_keys)


def is_currently_training(api_key):
    if api_key in currently_training_api_keys:
        if currently_training_api_keys[api_key].is_alive():
            return True
        else:
            del currently_training_api_keys[api_key]
    return False


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
    if is_currently_training(api_key):
        raise ClassifierIsAlreadyTrainingError
    logging.debug('Training started, api key: %s', api_key)
    classifier = get_trained_classifier(values, labels)
    process = Process(target=train, daemon=False, args=[api_key])
    currently_training_api_keys[api_key] = process
    logging.debug('Training finished, api key: %s', api_key)

    save_trained_model(api_key, TrainedModel(classifier=classifier, class_2_face_name=pred_class_to_face_name))


def train_async(api_key):
    global currently_training_api_keys
    process = Process(target=train, daemon=False, args=[api_key])
    if api_key in currently_training_api_keys and process.is_alive() == False:
        logging.debug('the api key is going to be removed')
        del currently_training_api_keys[api_key]

    if is_currently_training(api_key):
        logging.debug('we are not retraining')
        return False
    process.start()
    currently_training_api_keys[api_key] = process
    return True


def train_all_models():
    api_keys = get_storage().get_api_keys()
    if not api_keys:
        logging.warning("Face classifier training for all models hasn't been started, "
                        "because no API Keys were found in storage.")
        return

    for api_key in api_keys:
        train(api_key)
