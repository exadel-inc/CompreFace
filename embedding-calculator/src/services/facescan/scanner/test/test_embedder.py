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

import pytest

from sample_images import IMG_DIR
from src.services.facescan.scanner.facenet.facenet import Facenet2018
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.facescan.scanner.facescanners import TESTED_SCANNERS
from src.services.facescan.scanner.insightface.insightface import InsightFace
from src.services.facescan.scanner.test._scanner_cache import get_scanner
from src.services.utils.pyutils import first_and_only

DIFFERENCE_THRESHOLD = {
    InsightFace: 400,
    Facenet2018: 0.2
}


def embeddings_are_equal(embedding1, embedding2, difference_threshold):
    difference = sum(((a - b) ** 2 for a, b in zip(embedding1, embedding2)))
    print(f"Embedding difference: {difference}, difference threshold: {difference_threshold}")
    return difference < difference_threshold


@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', TESTED_SCANNERS)
def test__given_same_face_images__when_scanned__then_returns_same_embeddings(scanner_cls):
    scanner: FaceScanner = get_scanner(scanner_cls)
    img1 = IMG_DIR / '007_B.jpg'
    img2 = IMG_DIR / '008_B.jpg'

    emb1 = first_and_only(scanner.scan(img1)).embedding
    emb2 = first_and_only(scanner.scan(img2)).embedding

    assert embeddings_are_equal(emb1, emb2, DIFFERENCE_THRESHOLD[scanner_cls])


@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', TESTED_SCANNERS)
def test__given_diff_face_images__when_scanned__then_returns_diff_embeddings(scanner_cls):
    scanner: FaceScanner = get_scanner(scanner_cls)
    img1 = IMG_DIR / '007_B.jpg'
    img2 = IMG_DIR / '009_C.jpg'

    emb1 = first_and_only(scanner.scan(img1)).embedding
    emb2 = first_and_only(scanner.scan(img2)).embedding

    assert not embeddings_are_equal(emb1, emb2, DIFFERENCE_THRESHOLD[scanner_cls])
