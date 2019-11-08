import logging
from multiprocessing import Process

import toolz as toolz
from sklearn.linear_model import LogisticRegression

from src.storage.constants import EMBEDDING_CALCULATOR_MODEL_FILENAME
from src.storage.dto.embedding_classifier import EmbeddingClassifier
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

CLASSIFIER_VERSION = 'LogisticRegression'


def get_trained_model(values, labels):
    classifier = LogisticRegression(C=100000, solver='lbfgs', multi_class='multinomial')
    logging.debug('Training started')
    classifier.fit(values, labels)
    logging.debug('Training finished')
    return CLASSIFIER_VERSION, classifier


def train(api_key):
    # Load FaceEmbedding DTOs from DB
    storage = get_storage(api_key)
    faces = storage.get_face_embeddings(EMBEDDING_CALCULATOR_MODEL_FILENAME)
    if len(faces) <= 1:
        logging.warning("Not enough training data, model hasn't been created. Deleting existing models, if any.")
        storage.delete_embedding_classifiers()
        return
    unique_faces = list(toolz.unique(faces, lambda e: e.name))

    # Get embedding arrays
    embeddings = [face.embedding for face in unique_faces]
    embedding_calculator_version = embeddings[0].calculator_version
    assert all(embedding_calculator_version == embedding.calculator_version for embedding in embeddings)
    if is_currently_training(api_key):
        raise ClassifierIsAlreadyTrainingError
    logging.debug('Training started, api key: %s', api_key)
    classifier = get_trained_classifier(values, labels)
    process = Process(target=train, daemon=False, args=[api_key])
    currently_training_api_keys[api_key] = process
    logging.debug('Training finished, api key: %s', api_key)

    # Get trained model
    names = [face.name for face in unique_faces]
    embedding_arrays = [embedding.array for embedding in embeddings]
    classes = list(range(len(names)))
    model_version, model = get_trained_model(embedding_arrays, classes)

    # Create EmbeddingClassifier DTO and save it to DB
    class_2_face_name = {cls: name for cls, name in zip(classes, names)}
    classifier = EmbeddingClassifier(model_version, model, class_2_face_name, embedding_calculator_version)
    storage.save_embedding_classifier(classifier)


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
