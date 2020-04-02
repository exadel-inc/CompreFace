import os

_DO_RUN_TESTS = os.environ.get('DO_RUN_TESTS', 'false').lower() in ('true', '1')
MONGO_HOST = os.environ.get('MONGO_HOST', 'localhost')
MONGO_PORT = int(os.environ.get('MONGO_PORT', '27017'))
MONGO_EFRS_DATABASE_NAME = "efrs_e2e_db" if _DO_RUN_TESTS else "efrs_db"
