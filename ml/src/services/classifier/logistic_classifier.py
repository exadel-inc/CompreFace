from typing import Dict, List

import attr
import numpy as np
from sklearn.linear_model import LogisticRegression

from src.services.dto.face_prediction import NamePrediction
from src.services.imgtools.types import Array1D


@attr.s(auto_attribs=True, frozen=True)
class LogisticClassifier:
    CURRENT_VERSION = "LogisticClassifier_v0"

    model: LogisticRegression
    class_2_face_name: Dict[int, str]
    emb_calc_version: str
    version: str = CURRENT_VERSION

    @classmethod
    def train(cls, embeddings: List[Array1D], names: List[str], emb_calc_version: str):
        assert len(embeddings) == len(names)
        model = LogisticRegression(C=100000, solver='lbfgs', multi_class='multinomial')
        labels = list(range(len(names)))
        model.fit(X=embeddings, y=labels)
        class_2_face_name = {cls: name for cls, name in zip(labels, names)}
        return LogisticClassifier(model, class_2_face_name, emb_calc_version)

    def predict(self, embedding: Array1D, emb_calc_version: str):
        assert self.emb_calc_version == emb_calc_version
        probabilities = self.model.predict_proba([embedding])[0]
        top_class = np.argsort(-probabilities)[0]
        face_name = self.class_2_face_name[top_class]
        probability = probabilities[top_class]
        return NamePrediction(face_name, probability)
