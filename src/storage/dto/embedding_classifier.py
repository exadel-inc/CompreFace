from typing import Dict

import attr
from sklearn.linear_model import LogisticRegression


@attr.s(auto_attribs=True)
class EmbeddingClassifier:
    model: LogisticRegression
    class_2_face_name: Dict[int, str]
