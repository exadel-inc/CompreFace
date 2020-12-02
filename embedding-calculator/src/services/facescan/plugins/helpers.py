#  Copyright (c) 2020 the original author or authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

from importlib import import_module
from typing import List, Type, Tuple

from src import constants
from src.services.facescan.plugins.core import (BasePlugin, BaseFaceDetector,
                                                BaseCalculator)


ML_MODEL_SEPARATOR = '@'


def import_classes(class_path: str):
    module, class_name = class_path.rsplit('.', 1)
    return getattr(import_module(module, __package__), class_name)


def get_plugin_class_and_model(plugin_name) -> Tuple[Type[BasePlugin], str]:
    """
    Init a plugin by it's type. Additionally accepts a model type after '@'.

    >>> get_plugin_class_and_model("facenet.FaceDetector")
    (<class 'src.services.facescan.plugins.facenet.facenet.FaceDetector'>, None)

    >>> get_plugin_class_and_model("facenet.Calculator@20180408-102900")
    (<class 'src.services.facescan.plugins.facenet.facenet.Calculator'>, '20180408-102900')
    """
    ml_model_name = None
    if ML_MODEL_SEPARATOR in plugin_name:
        plugin_name, ml_model_name = plugin_name.split(ML_MODEL_SEPARATOR)

    plugin_class = import_classes(f'{__package__}.{plugin_name}')
    return plugin_class, ml_model_name


def get_plugins(plugins_names: List[str], type_filter: List[str] = None) -> List[BasePlugin]:
    plugins = []
    for plugin_name in plugins_names:
        plugin_class, model_name = get_plugin_class_and_model(plugin_name)
        if type_filter is not None and plugin_class.type not in type_filter:
            continue
        plugin = plugin_class(model_name)
        plugins.append(plugin)
    return plugins


def get_face_plugins(type_filter: List[str] = None):
    return get_plugins(
        [constants.ENV.CALCULATION_PLUGIN, *constants.ENV.EXTRA_PLUGINS],
        type_filter
    )


def get_calculator() -> BaseCalculator:
    return get_plugins([constants.ENV.CALCULATION_PLUGIN])[0]


def get_detector() -> BaseFaceDetector:
    return get_plugins([constants.ENV.FACE_DETECTION_PLUGIN])[0]
