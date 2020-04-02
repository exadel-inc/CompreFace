import logging
import os
import warnings

from tensorflow.python.util import deprecation as tensorflow_deprecation
from yaml import YAMLLoadWarning

from src.services.flaskext.logging_context import RequestContextLogFilter


class MainLogFilter(logging.Filter):
    def filter(self, record):
        if not hasattr(record, 'request'):
            record.request = ''
        record.column1 = f'{record.name}  {record.module}'
        record.column2 = f'[{record.levelname}]'
        return True


def init_logging():
    stream_handler = logging.StreamHandler()
    stream_handler.addFilter(RequestContextLogFilter())
    stream_handler.addFilter(MainLogFilter())
    log_format = (
        '%(asctime)s %(column1)-25s %(column2)10s'
        ' %(message)s'
        '%(request)s'
        ' [%(processName)s %(process)s %(threadName)s %(thread)d]'
        ' [%(pathname)s:%(lineno)d]')
    # noinspection PyArgumentList
    logging.basicConfig(level=logging.DEBUG,
                        format=log_format,
                        datefmt='%Y-%m-%d %H:%M:%S',
                        handlers=[stream_handler])

    logging.getLogger('PIL').setLevel(logging.INFO)
    logging.getLogger('werkzeug').setLevel(logging.ERROR)
    tensorflow_deprecation._PRINT_DEPRECATION_WARNINGS = False
    os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'
    warnings.filterwarnings("ignore", category=YAMLLoadWarning)
