import os

MONGO_HOST = os.environ.get('MONGO_HOST', 'mongo')
MONGO_PORT = int(os.environ.get('MONGO_PORT', '27017'))
