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
from time import time
from abc import ABC, abstractmethod
from pathlib import Path
from typing import List, Tuple, Optional
from zipfile import ZipFile

import attr
import gdown
from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.dto import plugin_result
from src.services.imgtools.types import Array3D
from src.services.facescan.plugins import exceptions


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
    def __call__(self, face_img: Array3D) -> plugin_result.PluginResultDTO:
        raise NotImplementedError


class BaseFaceDetector(BasePlugin):
    slug = 'detector'
    IMAGE_SIZE: int
    face_plugins: List[BasePlugin] = []

    def __call__(self, img: Array3D, det_prob_threshold: float = None,
                 face_plugins: Tuple[BasePlugin] = ()):
        """ Returns cropped and normalized faces."""
        faces = self._fetch_faces(img, det_prob_threshold)
        for face in faces:
            self._apply_face_plugins(face, face_plugins)
        return faces

    def _fetch_faces(self, img: Array3D, det_prob_threshold: float = None):
        start = time()
        boxes = self.find_faces(img, det_prob_threshold)
        return [
            plugin_result.FaceDTO(
                img=img, face_img=self.crop_face(img, box), box=box,
                execution_time={self.slug: (time() - start) / len(boxes)}
            ) for box in boxes
        ]

    def _apply_face_plugins(self, face: plugin_result.FaceDTO,
                            face_plugins: Tuple[BasePlugin]):
        for plugin in face_plugins:
            start = time()
            try:
                result_dto = plugin(face._face_img)
                face._plugins_dto.append(result_dto)
            except Exception as e:
                raise exceptions.PluginError(f'{plugin} error - {e}')
            else:
                face.execution_time[plugin.slug] = time() - start

    @abstractmethod
    def find_faces(self, img: Array3D, det_prob_threshold: float = None) -> List[BoundingBoxDTO]:
        """ Find face bounding boxes, without calculating embeddings"""
        raise NotImplementedError

    @abstractmethod
    def crop_face(self, img: Array3D, box: BoundingBoxDTO) -> Array3D:
        """ Crop face by bounding box and resize/squish it """
        raise NotImplementedError


class BaseCalculator(BasePlugin):
    slug = 'calculator'

    DIFFERENCE_THRESHOLD: float

    def __call__(self, face_img: Array3D):
        return plugin_result.EmbeddingDTO(
            embedding=self.calc_embedding(face_img)
        )

    @abstractmethod
    def calc_embedding(self, face_img: Array3D) -> Array3D:
        """ Calculate embedding of a given face """
        raise NotImplementedError
