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
from typing import Type, Union

from sample_images import IMG_DIR
from sample_images.annotations import SAMPLE_IMAGES
from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.facescan.scanner.facescanners import TESTED_SCANNERS
from src.services.facescan.scanner.test._cache import read_img
from src.services.facescan.scanner.test.calculate_errors import calculate_errors
from src.services.utils.pytestutils import is_sorted


@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', TESTED_SCANNERS)
def test__given_no_faces_img__when_scanned__then_returns_no_faces(scanner_cls):
    scanner: FaceScanner = scanner_cls()
    img = read_img(IMG_DIR / '017_0.jpg')

    result = scanner.scan(img)

    assert len(result) == 0


@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', TESTED_SCANNERS)
def test__given_5face_img__when_scanned__then_returns_5_correct_bounding_boxes_sorted_by_area(scanner_cls):
    correct_boxes = [BoundingBoxDTO(544, 222, 661, 361, 1),
                     BoundingBoxDTO(421, 236, 530, 369, 1),
                     BoundingBoxDTO(161, 36, 266, 160, 1),
                     BoundingBoxDTO(342, 160, 437, 268, 1),
                     BoundingBoxDTO(243, 174, 352, 309, 1)]
    scanner: FaceScanner = scanner_cls()
    img = read_img(IMG_DIR / '000_5.jpg')

    faces = scanner.scan(img)

    for face in faces:
        assert face.box.similar_to_any(correct_boxes, tolerance=20)
    assert is_sorted([face.box.width * face.box.height for face in faces])


@pytest.mark.integration
@pytest.mark.parametrize('scanner_cls', TESTED_SCANNERS)
def test__given_threshold_set_to_1__when_scanned__then_returns_no_faces(scanner_cls):
    scanner: FaceScanner = scanner_cls()
    img = read_img(IMG_DIR / '000_5.jpg')

    result = scanner.scan(img, det_prob_threshold=1)

    assert len(result) == 0


@pytest.mark.performance
@pytest.mark.parametrize('scanner_cls', TESTED_SCANNERS)
@pytest.mark.parametrize('row', (k for k in SAMPLE_IMAGES if k.include_to_tests))
def test__given_img__when_scanned__then_1_to_1_relationship_between_all_returned_boxes_and_faces(scanner_cls, row):
    scanner: FaceScanner = scanner_cls()
    img = read_img(IMG_DIR / row.img_name)

    scanned_faces = scanner.scan(img)

    assert calculate_errors(boxes=[face.box for face in scanned_faces], noses=row.noses) == 0
