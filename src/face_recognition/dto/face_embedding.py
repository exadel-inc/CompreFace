import attr
import numpy as np
from numpy.core.multiarray import ndarray


@attr.s(auto_attribs=True, cmp=False)
class Embedding:
    array: ndarray
    calculator_name: str

    def __eq__(self, other):
        if not isinstance(other, Embedding):
            raise NotImplementedError

        return (self.calculator_name == other.calculator_name
                and np.array_equal(self.array, other.array))

    def __ne__(self, other):
        return not self.__eq__(other)
