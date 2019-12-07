from _testbuffer import ndarray
from typing import List, NamedTuple, Tuple

Name = str
Image = ndarray
Datarows = List[Tuple[Name, Image]]


class Dataset(NamedTuple):
    train: Datarows
    test: Datarows
