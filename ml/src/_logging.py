import logging
import os
import warnings

from tensorflow.python.util import deprecation as tensorflow_deprecation
from yaml import YAMLLoadWarning

from src.constants import ENV
from src.services.flask_.logging_context import RequestContextLogFilter


class MainLogFilter(logging.Filter):
    def filter(self, record):
        if not hasattr(record, 'request'):
            record.request = ''

        if record.name == 'root':
            record.name = ''

        if not record.name.endswith(record.module):
            record.module = f'{record.module}.py'
        else:
            record.module = ''

        _metadata_elements = [record.request, record.name, record.module]
        if ENV.DO_LOG_MULTITASKING_IDS:
            _metadata_elements.extend([record.processName, record.process, record.threadName, record.thread])
        record.metadata = f"[{' '.join(str(k) for k in _metadata_elements if k)}]"
        return True


def init_logging(level):
    stream_handler = logging.StreamHandler()
    stream_handler.addFilter(RequestContextLogFilter())
    stream_handler.addFilter(MainLogFilter())
    if ENV.IS_DEV_ENV:
        log_format = f'%(asctime)s [%(levelname)s] %(message)s %(metadata)s'
    else:
        log_format = f'[%(levelname)s] %(message)s %(metadata)s'
    # noinspection PyArgumentList
    logging.basicConfig(level=level,
                        format=log_format,
                        datefmt='%Y-%m-%d %H:%M:%S',
                        handlers=[stream_handler])
    logging.getLogger('PIL').setLevel(logging.INFO)
    logging.getLogger('werkzeug').setLevel(logging.ERROR)
    warnings.filterwarnings("ignore", category=YAMLLoadWarning)
    tensorflow_deprecation._PRINT_DEPRECATION_WARNINGS = False
    os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'
    os.environ['MXNET_SUBGRAPH_VERBOSE'] = '0'
