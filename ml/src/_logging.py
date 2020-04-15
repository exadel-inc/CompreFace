import json
import logging
import os
import sys
import traceback
import warnings

import tensorflow as tf
from tensorflow.python.util import deprecation as tensorflow_deprecation
from yaml import YAMLLoadWarning

from src.constants import ENV
from src.services.flask_.logging_context import FlaskRequestContextAdder, request_dict_to_str


def init_logging(level):
    stream_handler = logging.StreamHandler()
    stream_handler.addFilter(FlaskRequestContextAdder())
    stream_handler.addFilter(TextFormatter() if ENV.IS_DEV_ENV else JSONFormatter())
    # noinspection PyArgumentList
    logging.basicConfig(level=level,
                        format='%(output)s',
                        datefmt='%Y-%m-%d %H:%M:%S',
                        handlers=[stream_handler])
    _set_logging_levels()


class TextFormatter(logging.Filter):
    def filter(self, record):
        request = request_dict_to_str(getattr(record, 'request_dict', None))
        logger = record.name if record.name != 'root' else ''
        module = f'{record.module}.py' if not logger.endswith(record.module) else ''

        metadata_elements = request, logger, module
        metadata = f"[{' '.join(str(k) for k in metadata_elements if k)}]"
        record.output = f'[{record.levelname}] {record.msg} {metadata}'
        return True


class JSONFormatter(logging.Filter):
    def filter(self, record):
        traceback_str = traceback.format_exc()
        record.output = json.dumps({
            'severity': record.levelname,
            'message': record.msg,
            'request': getattr(record, 'request_dict', None),
            'logger': record.name,
            'module': record.module,
            'traceback': traceback_str if sys.exc_info() != (None, None, None) else None
        })
        return True


def _set_logging_levels():
    logging.getLogger('PIL').setLevel(logging.INFO)
    logging.getLogger('werkzeug').setLevel(logging.ERROR)
    warnings.filterwarnings("ignore", category=YAMLLoadWarning)
    tensorflow_deprecation._PRINT_DEPRECATION_WARNINGS = False
    tf.compat.v1.logging.set_verbosity(tf.compat.v1.logging.ERROR)
    os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'
    os.environ['MXNET_SUBGRAPH_VERBOSE'] = '0'
