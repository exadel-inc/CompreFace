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

import os
import logging
import tempfile
from abc import ABC, abstractmethod
from pathlib import Path
from typing import List, Tuple, Optional
from zipfile import ZipFile

import attr
import gdown
from src.services.dto.json_encodable import JSONEncodable
from src.services.dto import plugin_result


logger = logging.getLogger(__name__)
MODELS_ROOT = os.path.expanduser(os.path.join('~', '.models'))


@attr.s(auto_attribs=True)
class MLModel:
    plugin: 'BasePlugin'
    name: str
    is_default: bool = False

    def __attrs_post_init__(self):
        """ Set first model as default """
        if not self.name:
            self.name = self.plugin.ml_models[0][0]
            self.is_default = True

    def __str__(self):
        return self.name

    @property
    def path(self):
        return Path(MODELS_ROOT) / self.plugin.backend / self.plugin.slug / self.name

    def exists(self):
        return os.path.exists(self.path)

    def download_if_not_exists(self):
        """
        Download a zipped model from url and extract it to models directory.
        """
        if self.exists():
            logger.debug(f'Already exists {self.plugin} model {self.name}')
            return
        logger.debug(f'Getting {self.plugin} model {self.name}')
        url = dict(self.plugin.ml_models)[self.name]
        with tempfile.NamedTemporaryFile() as tmpfile:
            self._download(url, tmpfile)
            self._extract(tmpfile.name)

    @classmethod
    def _download(cls, url: str, output):
        return gdown.download(cls._prepare_url(url), output)

    @staticmethod
    def _prepare_url(url) -> str:
        """ Convert Google Drive fileId to url """
        if not url.startswith('http') and len(url) < 40:
            return f'https://drive.google.com/uc?id={url}'
        return url

    def _extract(self, filename: str):
        os.makedirs(self.path, exist_ok=True)
        with ZipFile(filename, 'r') as zf:
            for info in zf.infolist():
                if info.is_dir():
                    continue
                file_path = Path(self.path) / Path(info.filename).name
                file_path.write_bytes(zf.read(info))


class BasePlugin(ABC):
    # pairs of model name and Google Drive fileID or URL to file
    ml_models: Tuple[Tuple[str, str], ...] = ()
    ml_model: Optional[MLModel] = None

    def __new__(cls, ml_model_name: str = None):
        """
        Plugins might cache pre-trained models and neural networks in properties
        so it has to be Singleton.
        """
        if not hasattr(cls, 'instance'):
            cls.instance = super(BasePlugin, cls).__new__(cls)
            if cls.instance.ml_models:
                cls.instance.ml_model = MLModel(cls.instance, ml_model_name)
        return cls.instance

    @property
    @abstractmethod
    def slug(self):
        pass

    @property
    def backend(self) -> str:
        return self.__class__.__module__.rsplit('.', 1)[-1]

    @property
    def name(self) -> str:
        return f'{self.backend}.{self.__class__.__name__}'

    def __str__(self):
        if self.ml_model and not self.ml_model.is_default:
            return f'{self.name}@{self.ml_model.name}'
        else:
            return self.name

    @abstractmethod
    def __call__(self, face: plugin_result.FaceDTO) -> JSONEncodable:
        raise NotImplementedError
