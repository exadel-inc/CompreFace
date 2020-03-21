import os

from src.services.utils.pyutils import run_once


@run_once
def get_scanner():
    from src.services.facescan.scanner import Scanner
    return Scanner.Facenet2018()


@run_once
def get_storage():
    from src.services.storage.mongo_storage import MongoStorage
    mongo_host = os.environ.get('MONGO_HOST', 'mongo')
    mongo_port = int(os.environ.get('MONGO_PORT', '27017'))
    return MongoStorage(mongo_host, mongo_port)


@run_once
def get_training_task_manager():
    from src.services.classifier.classifier_manager import TrainingTaskManager, train_and_save_classifier
    return TrainingTaskManager(train_and_save_classifier)
