import os

DO_DROP_DB = os.environ.get('DO_DROP_DB', '0').lower() in ('true', '1')
MONGO_HOST = os.environ.get('MONGO_HOST', 'mongo')
MONGO_PORT = int(os.environ.get('MONGO_PORT', '27017'))
MONGO_EFRS_DATABASE_NAME = os.environ.get('MONGO_EFRS_DATABASE_NAME', "efrs_db")
