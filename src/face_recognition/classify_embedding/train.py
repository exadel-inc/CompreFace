import logging
from typing import List

import toolz as toolz
from numpy.core.multiarray import ndarray
from sklearn.linear_model import LogisticRegression

from src.face_recognition.dto.embedding import Embedding
from src.storage.constants import EMBEDDING_CALCULATOR_MODEL_FILENAME
from src.storage.dto.embedding_classifier import EmbeddingClassifier
from src.storage.storage import get_storage

CLASSIFIER_VERSION = 'LogisticRegression'


def get_trained_model(values: List[ndarray], labels: List[int]):
    classifier = LogisticRegression(C=100000, solver='lbfgs', multi_class='multinomial')
    classifier.fit(values, labels)
    return CLASSIFIER_VERSION, classifier


def get_trained_classifier(embeddings: List[Embedding], names: List[str]) -> EmbeddingClassifier:
    embedding_calculator_version = embeddings[0].calculator_version
    assert all(embedding_calculator_version == embedding.calculator_version for embedding in embeddings)

    # Get trained model
    embedding_arrays = [embedding.array for embedding in embeddings]
    classes = list(range(len(names)))
    model_version, model = get_trained_model(embedding_arrays, classes)

    # Create EmbeddingClassifier DTO
    class_2_face_name = {cls: name for cls, name in zip(classes, names)}
    return EmbeddingClassifier(model_version, model, class_2_face_name, embedding_calculator_version)


def train_and_save_model(api_key):
    # Load FaceEmbedding DTOs from DB
    storage = get_storage(api_key)
    faces = storage.get_face_embeddings(EMBEDDING_CALCULATOR_MODEL_FILENAME)
    unique_faces = list(toolz.unique(faces, lambda e: e.name))
    if len(unique_faces) <= 1:
        logging.warning("Not enough training data, model hasn't been created. Deleting existing models, if any.")
        storage.delete_embedding_classifiers()
        return

    # Get embedding arrays
    embeddings = [face.embedding for face in faces]

    # Get trained classifier and save it to DB
    names = [face.name for face in faces]
    logging.debug("Training started for api_key, '%s'", api_key)
    classifier = get_trained_classifier(embeddings, names)
    logging.debug("Training finished for api_key, '%s'", api_key)
    storage.save_embedding_classifier(classifier)
