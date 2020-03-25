import os

MONGO_HOST = os.environ.get('MONGO_HOST', 'mongo')
MONGO_PORT = int(os.environ.get('MONGO_PORT', '27017'))
MONGO_EFRS_DATABASE_NAME = os.environ.get('MONGO_EFRS_DATABASE_NAME', "efrs_db")
DO_DROP_DB = os.environ.get('DO_DROP_DB', 'false').lower() in ('true', '1')
TIMEOUT_MULTIPLIER = float(os.environ.get('TIMEOUT_MULTIPLIER', '1'))
