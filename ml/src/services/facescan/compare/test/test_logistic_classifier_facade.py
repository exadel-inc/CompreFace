import json
import re

import pytest

from src.services.facescan.compare._dataset import Image
from src.services.facescan.compare._logistic_classifier_facade import LogisticClassifierFacade
from src.services.facescan.scanner.facescanners import FaceScanners


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
