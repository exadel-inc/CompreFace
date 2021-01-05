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

from typing import List, Tuple

import attr
import numpy as np

from src.services.dto.json_encodable import JSONEncodable


# noinspection PyUnresolvedReferences
@attr.s(auto_attribs=True, frozen=True)
class BoundingBoxDTO(JSONEncodable):
    """
    >>> BoundingBoxDTO(x_min=10, x_max=0, y_min=100, y_max=200, probability=0.5)
    Traceback (most recent call last):
    ...
    ValueError: 'x_min' must be smaller than 'x_max'
    """
    x_min: int = attr.ib(converter=int)
    y_min: int = attr.ib(converter=int)
    x_max: int = attr.ib(converter=int)
    y_max: int = attr.ib(converter=int)
    probability: float = attr.ib(converter=float)
    _np_landmarks: np.ndarray = attr.ib(factory=lambda: np.zeros(shape=(0, 2)),
                                        eq=False)

    @property
    def landmarks(self):
        return self._np_landmarks.astype(int).tolist()

    @x_min.validator
    def check_x_min(self, attribute, value):
        if value > self.x_max:
            raise ValueError("'x_min' must be smaller than 'x_max'")

    @y_min.validator
    def check_y_min(self, attribute, value):
        if value > self.y_max:
            raise ValueError("'y_min' must be smaller than 'y_max'")

    @probability.validator
    def check_probability(self, attribute, value):
        if not (0 <= value <= 1):
            raise ValueError("'probability' must be between 0 and 1")

    @property
    def xy(self):
        return (self.x_min, self.y_min), (self.x_max, self.y_max)

    @property
    def center(self):
        return (self.x_min + self.x_max) // 2, (self.y_min + self.y_max) // 2

    @property
    def width(self):
        return abs(self.x_max - self.x_min)

    @property
    def height(self):
        return abs(self.y_max - self.y_min)

    def similar(self, other: 'BoundingBoxDTO', tolerance: int) -> bool:
        """
        >>> BoundingBoxDTO(50,50,100,100,1).similar(BoundingBoxDTO(50,50,100,100,1),5)
        True
        >>> BoundingBoxDTO(50,50,100,100,1).similar(BoundingBoxDTO(50,50,100,95,1),5)
        True
        >>> BoundingBoxDTO(50,50,100,100,1).similar(BoundingBoxDTO(50,50,100,105,1),5)
        True
        >>> BoundingBoxDTO(50,50,100,100,1).similar(BoundingBoxDTO(50,50,100,94,1),5)
        False
        >>> BoundingBoxDTO(50,50,100,100,1).similar(BoundingBoxDTO(50,50,100,106,1),5)
        False
        """
        return (abs(self.x_min - other.x_min) <= tolerance
                and abs(self.y_min - other.y_min) <= tolerance
                and abs(self.x_max - other.x_max) <= tolerance
                and abs(self.y_max - other.y_max) <= tolerance)

    def similar_to_any(self, others: List['BoundingBoxDTO'], tolerance: int) -> bool:
        """
        >>> BoundingBoxDTO(50,50,100,100,1).similar_to_any([BoundingBoxDTO(50,50,100,105,1),\
                                                            BoundingBoxDTO(50,50,100,106,1)], 5)
        True
        >>> BoundingBoxDTO(50,50,100,100,1).similar_to_any([BoundingBoxDTO(50,50,100,106,1), \
                                                            BoundingBoxDTO(50,50,100,106,1)], 5)
        False
        """
        for other in others:
            if self.similar(other, tolerance):
                return True
        return False

    def is_point_inside(self, xy: Tuple[int, int]) -> bool:
        """
        >>> BoundingBoxDTO(100,700,150,750,1).is_point_inside((125,725))
        True
        >>> BoundingBoxDTO(100,700,150,750,1).is_point_inside((5,5))
        False
        """
        x, y = xy
        return self.x_min <= x <= self.x_max and self.y_min <= y <= self.y_max

    def scaled(self, coefficient: float) -> 'BoundingBoxDTO':
        # noinspection PyTypeChecker
        return BoundingBoxDTO(x_min=self.x_min * coefficient,
                              y_min=self.y_min * coefficient,
                              x_max=self.x_max * coefficient,
                              y_max=self.y_max * coefficient,
                              np_landmarks=self._np_landmarks * coefficient,
                              probability=self.probability)
