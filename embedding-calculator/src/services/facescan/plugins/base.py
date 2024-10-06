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
from typing import Any, Tuple, Optional
from zipfile import ZipFile

import attr
import gdown
from cached_property import cached_property

from src.services.dto.json_encodable import JSONEncodable
from src.services.dto import plugin_result


logger = logging.getLogger(__name__)
MODELS_ROOT = os.path.expanduser(os.path.join('~', '.models'))


@attr.s(auto_attribs=True)
class MLModel:
    plugin: 'BasePlugin'
    name: str
    google_drive_id: str

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
        with tempfile.NamedTemporaryFile() as tmpfile:
            self._download(self.url, tmpfile)
            self._extract(tmpfile.name)

    @property
    def url(self):
        return f'https://drive.google.com/uc?id={self.google_drive_id}'

    @classmethod
    def _download(cls, url: str, output):
        return gdown.download(url, output)

    def _extract(self, filename: str):
        os.makedirs(self.path, exist_ok=True)
        with ZipFile(filename, 'r') as zf:
            if self.plugin.retain_folder_structure:
                for info in zf.infolist():
                    if info.is_dir():
                        os.makedirs(Path(self.path) / Path(info.filename))
                        continue
                    file_path = Path(self.path) / Path(info.filename)
                    file_path.write_bytes(zf.read(info))
            else:
                for info in zf.infolist():
                    if info.is_dir():
                        continue
                    file_path = Path(self.path) / Path(info.filename).name
                    file_path.write_bytes(zf.read(info))


@attr.s(auto_attribs=True)
class CalculatorModel(MLModel):
    # used to convert euclidean distance to similarity [0.0..1.0]
    # E.g. algorithm: (tanh((first_coef - distance) * second_coef) + 1) / 2
    similarity_coefficients: Tuple[float, float] = (0, 1)
    difference_threshold: float = 0.4


class BasePlugin(ABC):
    # args for init MLModel: model name, Goodle Drive fileID
    ml_models: Tuple[Tuple[str, str], ...] = ()
    ml_model_name: str = None

    def __new__(cls, ml_model_name: str = None):
        """
        Plugins might cache pre-trained models and neural networks in properties
        so it has to be Singleton.
        """
        if not hasattr(cls, 'instance'):
            cls.instance = super(BasePlugin, cls).__new__(cls)
            cls.instance.ml_model_name = ml_model_name
        return cls.instance

    @property
    @abstractmethod
    def slug(self):
        pass

    def create_ml_model(self, *args):
        """ Create MLModel instance by arguments following plugin settings """
        return MLModel(self, *args)

    @cached_property
    def ml_model(self) -> Optional[MLModel]:
        if hasattr(self, 'ml_models'):
            for ml_model_args in self.ml_models:
                if not self.ml_model_name or self.ml_model_name == ml_model_args[0]:
                    return self.create_ml_model(*ml_model_args)

    @property
    def backend(self) -> str:
        return self.__class__.__module__.rsplit('.', 1)[-1]

    @property
    def name(self) -> str:
        return f'{self.backend}.{self.__class__.__name__}'

    @property
    def retain_folder_structure(self) -> bool:
        return False

    def __str__(self):
        if self.ml_model and self.ml_model_name:
            return f'{self.name}@{self.ml_model_name}'
        else:
            return self.name

    @abstractmethod
    def __call__(self, face: plugin_result.FaceDTO) -> JSONEncodable:
        raise NotImplementedError
