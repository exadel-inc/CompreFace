import logging
from typing import List
from typing import Tuple

from src.services.dto.bounding_box import BoundingBoxDTO

logger = logging.getLogger(__name__)


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
    missed_noses = sum(1 != sum(box.is_point_inside(nose) for box in boxes) for nose in noses)
    missed_boxes = sum(1 != sum(box.is_point_inside(nose) for nose in noses) for box in boxes)
    return missed_noses + missed_boxes
