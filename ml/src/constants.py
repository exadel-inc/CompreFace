import os


class ENV:
    ML_PORT = int(os.environ.get('ML_PORT', '3000'))

    @classmethod
    def dict(cls):
        return {key: cls.__dict__[key] for key in cls.__dict__.keys() if not key.startswith('_')}
