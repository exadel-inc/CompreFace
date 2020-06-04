import logging
from functools import total_ordering
from typing import List, Set

import numpy as np

from sample_images import IMG_DIR
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.imgtools.read_img import read_img
from src.services.imgtools.types import Array1D
from src.services.utils.pyutils import get_current_dir
from tools.facescan.constants import ENV_BENCHMARK

TMP_DIR = get_current_dir(__file__) / 'tmp'
logger = logging.getLogger(__name__)


@total_ordering
class Image:
    _total_embeddings_calculated = 0

    def __init__(self, name, number, path):
        self.name = name
        self.number = number
        self._path = path
        self._array = None
        self._embeddings = {}

    @classmethod
    def from_lfw_path(cls, path):
        name = path.parts[-2]
        number = int(''.join(k for k in path.parts[-1] if k.isdigit()))
        return cls(name, number, path)

    @classmethod
    def from_sample_images(cls, image_name):
        filename_without_extension = image_name.split('.')[0]
        number_str, name = filename_without_extension.split('_')
        number = int(number_str)
        return cls(name, number, IMG_DIR / image_name)

    @property
    def array(self):
        if self._array is None:
            self._array = read_img(self._path) if not ENV_BENCHMARK.DRY_RUN else np.random.rand(1)
        return self._array

    def embedding(self, scanner: FaceScanner) -> Array1D:
        if scanner.ID not in self._embeddings:
            Image._total_embeddings_calculated += 1
            if Image._total_embeddings_calculated % 1 == 0:
                logger.debug(f"Calculating embedding #{Image._total_embeddings_calculated}: {self.__repr__()}")
            self._embeddings[scanner.ID] = scanner.scan_one(self.array).embedding
        return self._embeddings[scanner.ID]

    def __hash__(self):
        return hash((self.name, self.number))

    def __repr__(self):
        return f"{self.name} (#{self.number}) {{{len(self._embeddings)}}}"

    def __eq__(self, other):
        return (self.name, self.number) == (other.name, other.number)

    def __lt__(self, other):
        return (self.name, self.number) < (other.name, other.number)


def get_lfw_dataset() -> Set[Image]:
    image_paths = sorted(TMP_DIR.glob('lfw/*/*.jpg'))
    images = set(Image.from_lfw_path(path) for path in image_paths)
    assert len(images) == 13233
    return images


def get_people_txt_folds(lfw_dataset: Set[Image]) -> List[Set[Image]]:
    image_dict = {(img.name, img.number): img for img in lfw_dataset}
    folds = []
    with (TMP_DIR / 'people.txt').open('r') as f:
        fold_count = int(next(f))
        assert fold_count == 10
        for _ in range(fold_count):
            fold = []
            images_count = int(next(f))
            for _ in range(images_count):
                line = next(f)
                name, number_str = [k.strip() for k in line.replace('\t', ' ').split()]
                fold.append(image_dict[(name, int(number_str))])
            folds.append(set(fold))
    return folds
