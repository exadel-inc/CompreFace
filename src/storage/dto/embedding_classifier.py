from typing import Dict, Union

import attr
from sklearn.linear_model import LogisticRegression


@attr.s(auto_attribs=True)
class EmbeddingClassifier:
    name: str
    model: Union[object, LogisticRegression]
    class_2_face_name: Dict[int, str]
    embedding_calculator_name: str
