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

from sample_images import IMG_DIR, annotations
from src.services.facescan.plugins.managers import plugin_manager
from src.services.facescan.plugins.agegender.agegender import AgeDetector, GenderDetector
from src.services.facescan.scanner.test._cache import read_img


age_detector = plugin_manager.get_plugin_by_class(AgeDetector)
gender_detector = plugin_manager.get_plugin_by_class(GenderDetector)


@pytest.mark.skipif(not any([age_detector, gender_detector]),
                    reason="Disabled age/gender plugins")
@pytest.mark.performance
@pytest.mark.parametrize('img_name', annotations.name_2_person)
def test_getting_age_and_gender(img_name: str):

    img = read_img(IMG_DIR / img_name)
    person = annotations.name_2_person[img_name]
    face = plugin_manager.detector(img)[0]

    if age_detector and img_name != '006_A.jpg':
        age_range = age_detector(face).age
        assert age_range['low'] <= person.age <= age_range['high'], \
            f'{img_name}: Age mismatched: {person.age} not in  {age_range}'

    if gender_detector:
        gender = gender_detector(face).gender
        assert gender is not None
        assert (gender['value'] == 'male') == person.is_male, \
            f'{img_name}: Wrong gender - {gender}'
