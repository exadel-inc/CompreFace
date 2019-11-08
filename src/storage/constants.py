import os

EMBEDDING_CALCULATOR_MODEL_FILENAME = "embedding_calc_model_20170512.pb"
MONGO_EFRS_DATABASE_NAME = "efrs_db"
MONGO_HOST = os.environ.get('MONGO_HOST', 'mongo'),
MONGO_PORT = int(os.environ.get('MONGO_PORT', '27017'))
