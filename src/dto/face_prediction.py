from typing import NamedTuple

from src.dto import BoundingBox


class FacePrediction(NamedTuple):
    box: BoundingBox
    prediction: str
    probability: float
