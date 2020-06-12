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

from src.exceptions import NoFileFoundInDatabaseError
from src.services.storage.mongo_storage import MongoStorage
from src.services.utils.pytestutils import raises

FILENAME = 'filename.bin'


def test__given_no_saved_file__when_getting_file__then_raises_error(storage: MongoStorage):
    pass  # NOSONAR

    def act():
        storage.get_file(FILENAME)

    assert raises(NoFileFoundInDatabaseError, act)


def test__given_saved_file__when_getting_file__then_returns_file(storage: MongoStorage):
    storage.save_file(FILENAME, b'hello')

    bytes_data = storage.get_file(FILENAME)

    assert bytes_data == b'hello'
