import logging
from threading import Thread

import toolz as toolz
from sklearn.linear_model import LogisticRegression

from src.storage.constants import EMBEDDING_CALCULATOR_MODEL_FILENAME
from src.storage.dto.embedding_classifier import EmbeddingClassifier
from src.storage.storage import get_storage

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

    # Get trained model
    names = [face.name for face in unique_faces]
    embedding_arrays = [embedding.array for embedding in embeddings]
    classes = list(range(1, len(names) + 1))
    model_version, model = get_trained_model(embedding_arrays, classes)

    # Create EmbeddingClassifier DTO and save it to DB
    class_2_face_name = {cls: name for cls, name in zip(classes, names)}
    classifier = EmbeddingClassifier(model_version, model, class_2_face_name, embedding_calculator_version)
    storage.save_embedding_classifier(classifier)


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
