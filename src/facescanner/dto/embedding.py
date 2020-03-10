import attr
import numpy as np
from numpy.core.multiarray import ndarray


@attr.s(auto_attribs=True, frozen=True, cmp=False)
class Embedding:
    """
    >>> arr1 , arr2, arr3 = np.zeros(shape=(1,)), np.ones(shape=(1,)), np.zeros(shape=(2,))
    >>> Embedding(arr1, 'e') == Embedding(arr1, 'e')
    True
    >>> hash(Embedding(arr1, 'e')) == hash(Embedding(arr1, 'e'))
    True
    >>> Embedding(arr1, 'e') != Embedding(arr1, 'e')
    False
    >>> Embedding(arr1, 'e') == Embedding(arr2, 'e')
    False
    >>> Embedding(arr1, 'e') == Embedding(arr3, 'e')
    False
    >>> Embedding(arr1, 'e') == Embedding(arr1, 'm')
    False
    """
    array: ndarray
    calculator_version: str

    def __hash__(self):
        return (hash(self.array.data.tobytes())
                ^ hash(self.calculator_version))

    def __eq__(self, other):
        if not isinstance(other, Embedding):
            raise NotImplementedError

        return (self.calculator_version == other.calculator_version
                and np.array_equal(self.array, other.array))
