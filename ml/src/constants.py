import os

_use_test_db = os.environ.get('DO_RUN_TESTS', 'false').lower() in ('true', '1')


class ENV:
    ML_PORT = int(os.environ.get('ML_PORT', '3000'))
    MONGO_HOST = os.environ.get('MONGO_HOST', 'mongo')
    MONGO_PORT = int(os.environ.get('MONGO_PORT', '27017'))
    MONGO_EFRS_DATABASE_NAME = "efrs_e2e_db" if _use_test_db else "efrs_db"

    @classmethod
    def dict(cls):
        return {key: cls.__dict__[key] for key in cls.__dict__.keys() if not key.startswith('_')}
