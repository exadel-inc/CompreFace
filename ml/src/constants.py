import os

DO_SHOW_STACKTRACE_IN_LOGS = os.environ.get('DO_SHOW_STACKTRACE_IN_LOGS', 'true').lower() in ('true', '1')
DO_SHOW_HTTP_RESPONSES_IN_LOGS = os.environ.get('DO_SHOW_HTTP_RESPONSES_IN_LOGS', 'true').lower() in ('true', '1')
