import logging
from threading import Thread

from sklearn.linear_model import LogisticRegression

from src.dto.trained_model import TrainedModel
from src.storage.model_storage import save_model, delete_model
from src.storage.storage_factory import get_storage


def train(api_key):
    values, labels, pred_class_to_face_name = get_storage().get_classifier_training_data(api_key)
    if len(pred_class_to_face_name) <= 1:
        logging.warning("Not enough training data, model hasn't been created")
        delete_model(api_key)
        return

    logging.debug('Training classifier, api key: %s' % api_key)
    classifier = LogisticRegression(C=100000, solver='lbfgs', multi_class='multinomial')
    classifier.fit(values, labels)
    logging.debug('Training finished, api key: %s' % api_key)

    save_model(api_key, TrainedModel(classifier=classifier, class_2_face_name=pred_class_to_face_name))


def train_async(api_key):
    thread = Thread(target=train, daemon=False, args=[api_key])
    thread.start()
    return thread


def train_all_models():
    api_keys = get_storage().get_api_keys()
    if not api_keys:
        logging.warning("Face classifier training for all models hasn't been started, "
                        "because no API Keys were found in storage.")
        return

    for api_key in api_keys:
        train(api_key)
