import collections
import copy
from typing import Tuple, Generator, TypeVar, Iterable, Dict, List

import numpy as np
import pytest
from numpy.core.multiarray import ndarray
from sklearn.datasets import fetch_lfw_people

FaceName = int
Image = ndarray
X, Y = TypeVar('X'), TypeVar('Y')
Dataset = Dict[X, List[Y]]


def get_lfw_data_rows() -> Generator[Tuple[FaceName, Image], None, None]:
    lfw_people = fetch_lfw_people(min_faces_per_person=120, color=True, funneled=False)
    count, img_height, img_width, img_colors = lfw_people.images.shape
    for i in range(count):
        img = lfw_people.images[i, :, :, :].reshape((img_height, img_width, img_colors))
        yield lfw_people.target[i], img.astype(np.uint8)


def create_dataset_from_data_rows(data_rows: Iterable[Tuple[X, Y]]) -> Dataset:
    """
    >>> dict(create_dataset_from_data_rows([(1, 'a'), (1, 'b'), (2, 'c')]))
    {1: ['a', 'b'], 2: ['c']}
    """
    x2y = collections.defaultdict(list)
    for xi, yi in data_rows:
        x2y[xi].append(yi)
    return x2y


def split_dataset_train_test(dataset: Dataset) -> Tuple[Dataset, Dataset]:
    """
    >>> tuple(dict(d) for d in split_dataset_train_test({1: ['a', 'b'], 2: ['c']}))
    ({1: ['a'], 2: ['c']}, {1: ['b']})
    """
    dataset_train = copy.deepcopy(dataset)
    dataset_test = collections.defaultdict(list)
    for x in dataset:
        if len(dataset[x]) < 2:
            continue
        dataset_test[x].append(dataset_train[x].pop())
    return dataset_train, dataset_test


if __name__ == '__main__':
    assert pytest.main(['-qq', '--doctest-modules']) == 0
    lfw_data_rows = get_lfw_data_rows()
    lfw_dataset = create_dataset_from_data_rows(lfw_data_rows)
    lfw_dataset_train, lfw_dataset_test = split_dataset_train_test(lfw_dataset)
    ...
