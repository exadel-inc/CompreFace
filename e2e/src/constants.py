import os

MONGO_HOST = os.environ.get('MONGO_HOST', 'localhost')
MONGO_PORT = int(os.environ.get('MONGO_PORT', '27017'))
MONGO_EFRS_DATABASE_NAME = "efrs_e2e_db"
