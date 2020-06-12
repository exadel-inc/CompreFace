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

import attr


@attr.s(auto_attribs=True)
class SimpleStats:
    scanner_name: str
    total_boxes: int = 0
    total_missed_boxes: int = 0
    total_noses: int = 0
    total_missed_noses: int = 0

    def add(self, total_boxes, total_missed_boxes, total_noses, total_missed_noses):
        self.total_boxes += total_boxes
        self.total_missed_boxes += total_missed_boxes
        self.total_noses += total_noses
        self.total_missed_noses += total_missed_noses

    def __str__(self, infix=False):
        infix = f'[{infix}] ' if infix else ""
        return (f"{infix}"
                f"Undetected faces: {self.total_missed_noses}/{self.total_noses}, "
                f"False face detections: {self.total_missed_boxes}/{self.total_boxes}")
