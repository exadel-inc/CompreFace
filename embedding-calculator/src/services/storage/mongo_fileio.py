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

from gridfs import GridFS

from src.exceptions import NoFileFoundInDatabaseError


def save_file_to_mongo(files_fs: GridFS, filename: str, bytes_data: bytes):
    result = files_fs.find_one({"filename": filename})
    if result:
        files_fs.delete(result['_id'])

    files_fs.put(bytes_data, filename=filename)


def get_file_from_mongo(files_fs: GridFS, filename: str):
    result = files_fs.find_one({"filename": filename})
    if result is None:
        raise NoFileFoundInDatabaseError(f'File with filename {filename} is not found in the database')
    return result.read()
