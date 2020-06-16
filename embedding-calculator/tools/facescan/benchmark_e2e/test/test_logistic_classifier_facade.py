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

import json
import re

import pytest

from src.services.facescan.scanner.facescanners import FaceScanners
from tools.facescan.benchmark_e2e._dataset import Image
from tools.facescan.benchmark_e2e._logistic_classifier_facade import LogisticClassifierFacade


def _remove_whitespace(string):
    return re.sub(r"\s+", '', string, flags=re.UNICODE)


@pytest.mark.integration
def test__when_testing__returns_correct_results():
    train_image_names = '005_A.jpg', '007_B.jpg', '000_5.jpg'
    test_image_names = '006_A.jpg', '008_B.jpg', '009_C.jpg'
    train_dataset = {Image.from_sample_images(name) for name in train_image_names}
    test_dataset = {Image.from_sample_images(name) for name in test_image_names}
    scanner = FaceScanners.Facenet2018()
    tester = LogisticClassifierFacade(scanner)

    tester.train(train_dataset)
    results = tester.test_recognition(test_dataset)

    expected_json = """
    {
        "Prediction": {
            "ImagesGuessedCorrectly": 2,
            "ImagesTotal": 3,
            "NotInGivenTrainNamesError": 1
        },
        "Training": {
            "ImagesTotal": 3,
            "ImagesTrained": 2,
            "MoreThanOneFaceFoundError": 1,
            "NamesTrained": 2
        }
    }
    """
    assert _remove_whitespace(json.dumps(results, sort_keys=True)) == _remove_whitespace(expected_json)
