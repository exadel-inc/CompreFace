from typing import Dict

import attr

from src.types.classifier import Classifier


@attr.s(auto_attribs=True)
class TrainedModel:
    classifier: Classifier
    class_2_face_name: Dict[int, str]
