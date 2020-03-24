import os

DO_SHOW_STACKTRACE_IN_LOGS = os.environ.get('DO_SHOW_STACKTRACE_IN_LOGS', '0').lower() in ('true', '1')
DO_SHOW_HTTP_RESPONSES_IN_LOGS = os.environ.get('DO_SHOW_HTTP_RESPONSES_IN_LOGS', '0').lower() in ('true', '1')
MONGO_HOST = os.environ.get('MONGO_HOST', 'mongo')
MONGO_PORT = int(os.environ.get('MONGO_PORT', '27017'))
MONGO_EFRS_DATABASE_NAME = os.environ.get('MONGO_EFRS_DATABASE_NAME', "efrs_db")
