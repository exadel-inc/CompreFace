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

from scipy.spatial.distance import euclidean

from sample_images import IMG_DIR
from sample_images.annotations import SAMPLE_IMAGES
from src.services.facescan.scanner.test._cache import read_img
from src.services.facescan.plugins import mixins
from src.services.facescan.plugins.managers import plugin_manager


landmarks_plugins = [pl for pl in plugin_manager.face_plugins
                     if isinstance(pl, mixins.LandmarksDetectorMixin)]


@pytest.mark.performance
@pytest.mark.parametrize('plugin', landmarks_plugins, ids=str)
@pytest.mark.parametrize('row', SAMPLE_IMAGES, ids=str)
def test_landmarks(plugin: mixins.LandmarksDetectorMixin, row):
    img = read_img(IMG_DIR / row.img_name)

    faces = plugin_manager.detector(img=img, face_plugins=[plugin])
    sorted_faces = sorted(faces, key=lambda x: x._plugins_dto[0].nose)

    for face, excepted_nose in zip(sorted_faces, sorted(row.noses)):
        landmarks = face._plugins_dto[0]
        tolerance = face.box.height * 0.07
        assert euclidean(landmarks.nose, excepted_nose) < tolerance
