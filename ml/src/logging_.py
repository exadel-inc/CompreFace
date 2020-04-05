import logging
import os
import sys
import warnings

from tensorflow.python.util import deprecation as tensorflow_deprecation
from yaml import YAMLLoadWarning

from src.constants import ENV
from src.services.flask_.logging_context import RequestContextLogFilter


class MainLogFilter(logging.Filter):
    def filter(self, record):
        if not hasattr(record, 'request'):
            record.request = ''
        logger_name = f"{record.name} " if record.name != 'root' else ""
        record.module = f"{logger_name}{record.module}"
        return True


def init_logging():
    stream_handler = logging.StreamHandler()
    stream_handler.addFilter(RequestContextLogFilter())
    stream_handler.addFilter(MainLogFilter())
    log_format = ('%(levelname)-8s%(asctime)s  |  %(message)s  |  %(request)s%(module)s'
                  ' %(processName)s %(process)s %(threadName)s %(thread)d')
    # noinspection PyArgumentList
    logging.basicConfig(level=logging.DEBUG,
                        format=log_format,
                        datefmt='%Y-%m-%d %H:%M:%S',
                        handlers=[stream_handler])
    logging.getLogger('PIL').setLevel(logging.INFO)
    logging.getLogger('werkzeug').setLevel(logging.ERROR)
    warnings.filterwarnings("ignore", category=YAMLLoadWarning)
    tensorflow_deprecation._PRINT_DEPRECATION_WARNINGS = False
    os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'
    os.environ['MXNET_SUBGRAPH_VERBOSE'] = '0'


def init_runtime():
    assert sys.version_info >= (3, 7)
    init_logging()
    logging.info(ENV.__str__())
