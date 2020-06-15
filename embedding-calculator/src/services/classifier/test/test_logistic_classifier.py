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

import joblib
import pytest

from src.services.classifier.logistic_classifier import LogisticClassifier
from src.services.utils.pyutils import get_current_dir

CURRENT_DIR = get_current_dir(__file__)


def load_embedding(name):
    return joblib.load(CURRENT_DIR / f'{name}.embedding.joblib')


@pytest.mark.integration
def test__given_2_embeddings__when_trained__then_correctly_classifies_3rd_embedding():
    embedding_a = load_embedding('01.A')
    embedding_b = load_embedding('02.A')
    new_embedding = load_embedding('07.B')
    embeddings = [embedding_a, embedding_b]
    names = ['Person A', 'Person B']
    emb_calc_version = 'cached_joblib'

    classifier = LogisticClassifier.train(embeddings, names, emb_calc_version)
    prediction = classifier.predict(new_embedding, emb_calc_version)

    assert prediction.face_name == 'Person A'
