from typing import List

import attr
import numpy as np

from src.pyutils.convertible_to_dict import ConvertibleToDict


@attr.s(auto_attribs=True, frozen=True)
class Embedding(ConvertibleToDict):
    array: List[float] = attr.ib(converter=lambda x: x.tolist() if isinstance(x, np.ndarray) else x)
    calculator_version: str
