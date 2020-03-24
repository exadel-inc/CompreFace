import os

MONGO_HOST = os.environ.get('MONGO_HOST', 'mongo')
MONGO_PORT = int(os.environ.get('MONGO_PORT', '27017'))
MONGO_EFRS_DATABASE_NAME = os.environ.get('MONGO_EFRS_DATABASE_NAME', "efrs_db")
DO_SHOW_STACKTRACE_IN_LOGS = os.environ.get('DO_SHOW_STACKTRACE_IN_LOGS', 'true').lower() in ('true', '1')
DO_SHOW_HTTP_RESPONSES_IN_LOGS = os.environ.get('DO_SHOW_HTTP_RESPONSES_IN_LOGS', 'true').lower() in ('true', '1')
