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

import logging
from typing import List
from typing import Tuple

from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.utils.pyutils import get_nearest_point_idx

logger = logging.getLogger(__name__)


def calculate_missed_noses(boxes: List[BoundingBoxDTO], noses: List[Tuple[int, int]]):
    """
    >>> calculate_missed_noses([BoundingBoxDTO(x_min=283, y_min=31, x_max=376, y_max=142, probability=1), \
                                BoundingBoxDTO(x_min=174, y_min=90, x_max=280, y_max=211, probability=1), \
                                BoundingBoxDTO(x_min=246, y_min=60, x_max=333, y_max=167, probability=1)], \
                               [(232, 143), (291, 108), (341, 76)])
    0
    >>> calculate_missed_noses([], [(232, 143), (291, 108), (341, 76)])
    3
    >>> calculate_missed_noses([BoundingBoxDTO(x_min=283, y_min=31, x_max=376, y_max=142, probability=1), \
                                BoundingBoxDTO(x_min=174, y_min=90, x_max=280, y_max=211, probability=1), \
                                BoundingBoxDTO(x_min=246, y_min=60, x_max=333, y_max=167, probability=1)], \
                               [])
    0
    """
    missed_noses = noses[:]
    boxes = boxes[:]
    for nose_idx, nose in enumerate(noses):
        if not boxes:
            break
        box_idx = get_nearest_point_idx(nose, [box.center for box in boxes])
        if boxes[box_idx].is_point_inside(nose):
            # noinspection PyTypeChecker
            missed_noses[nose_idx] = None
            boxes.pop(box_idx)
    return sum(1 for k in missed_noses if k)


def calculate_missed_boxes(boxes: List[BoundingBoxDTO], noses: List[Tuple[int, int]]):
    """
    >>> calculate_missed_boxes([BoundingBoxDTO(x_min=283, y_min=31, x_max=376, y_max=142, probability=1), \
                                BoundingBoxDTO(x_min=174, y_min=90, x_max=280, y_max=211, probability=1), \
                                BoundingBoxDTO(x_min=246, y_min=60, x_max=333, y_max=167, probability=1)], \
                               [(232, 143), (291, 108), (341, 76)])
    0
    >>> calculate_missed_boxes([], [(232, 143), (291, 108), (341, 76)])
    0
    >>> calculate_missed_boxes([BoundingBoxDTO(x_min=283, y_min=31, x_max=376, y_max=142, probability=1), \
                                BoundingBoxDTO(x_min=174, y_min=90, x_max=280, y_max=211, probability=1), \
                                BoundingBoxDTO(x_min=246, y_min=60, x_max=333, y_max=167, probability=1)], \
                               [])
    3
    """
    missed_boxes = boxes[:]
    noses = noses[:]
    for box_idx, box in enumerate(boxes):
        if not noses:
            break
        nose_idx = get_nearest_point_idx(box.center, noses)
        if box.is_point_inside(noses[nose_idx]):
            # noinspection PyTypeChecker
            missed_boxes[box_idx] = None
            noses.pop(nose_idx)
    return sum(1 for k in missed_boxes if k)


def calculate_errors(boxes: List[BoundingBoxDTO], noses: List[Tuple[int, int]]):
    """
    >>> calculate_errors([BoundingBoxDTO(100,500,200,600,1)], [(150,550)])
    0
    >>> calculate_errors([BoundingBoxDTO(100,500,200,600,1), BoundingBoxDTO(100,500,200,600,1)], [(150,550)])
    1
    >>> calculate_errors([BoundingBoxDTO(100,500,200,600,1)], [(150,550),(150,550)])
    1
    >>> calculate_errors([BoundingBoxDTO(100,500,200,600,1)], [(1150,1550)])
    2
    >>> calculate_errors([BoundingBoxDTO(100,500,200,600,1)], [])
    1
    >>> calculate_errors([], [(150,550)])
    1
    """
    return calculate_missed_noses(boxes, noses) + calculate_missed_boxes(boxes, noses)
