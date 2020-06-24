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
from functools import lru_cache

from src.services.imgtools.read_img import read_img


@lru_cache(maxsize=None)
def get_scanner(scanner_cls):
    scanner = scanner_cls()

    @lru_cache(maxsize=None)
    def scan(img_path, *args, **kwargs):
        img = read_img(img_path)
        return scanner.scan_(img, *args, **kwargs)

    scanner.scan_ = scanner.scan
    scanner.scan = scan
    return scanner
