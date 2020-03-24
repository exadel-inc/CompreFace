import logging
import warnings

from tensorflow.python.util import deprecation as tensorflow_deprecation
from yaml import YAMLLoadWarning

from src.services.flaskext.logging_filter import ApiKeyLogFilter


class MainLogFilter(logging.Filter):
    def filter(self, record):
        if not hasattr(record, 'api_key'):
            record.api_key = ''
        record.column1 = f'{record.levelname}'
        record.column2 = f'{record.name}  {record.module}'
        return True


def init_logging():
    stream_handler = logging.StreamHandler()
    stream_handler.addFilter(ApiKeyLogFilter())
    stream_handler.addFilter(MainLogFilter())
    # noinspection PyArgumentList
    logging.basicConfig(level=logging.DEBUG,
                        format='%(asctime)s %(column1)-9s %(column2)-35s'
                               ' %(message)-130s'
                               '%(api_key)s'
                               ' [%(processName)s %(process)s %(threadName)s %(thread)d]'
                               ' [%(pathname)s:%(lineno)d]',
                        datefmt='%Y-%m-%d %H:%M:%S',
                        handlers=[stream_handler])
    logging.getLogger('PIL').setLevel(logging.INFO)
    tensorflow_deprecation._PRINT_DEPRECATION_WARNINGS = False
    warnings.filterwarnings("ignore", category=YAMLLoadWarning)
