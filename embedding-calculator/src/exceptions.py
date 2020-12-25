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

from werkzeug.exceptions import BadRequest, Locked, InternalServerError, Unauthorized

from src.constants import ENV


class APIKeyNotSpecifiedError(Unauthorized):
    description = 'No API Key is given'


class NoFileAttachedError(BadRequest):
    description = "No file is attached"


class NoFileSelectedError(BadRequest):
    description = "No file is selected"


class NoFaceFoundError(BadRequest):
    description = "No face is found in the given image"


class OneDimensionalImageIsGivenError(BadRequest):
    description = "Given image has only one dimension"


class ClassifierIsAlreadyTrainingError(Locked):
    description = "Classifier training is already in progress"


class NoFileFoundInDatabaseError(InternalServerError):
    description = "File is not found in the database"


class InvalidRequestArgumentValueError(BadRequest):
    description = "Invalid request argument value is given"


class ImageReadLibraryError(BadRequest):
    description = "Image has incorrect format or is broken"


class FaceHasNoEmbeddingCalculatedError(InternalServerError):
    description = "Saved face has no embedding calculated and saved in the database"


class CouldNotConnectToDatabase(InternalServerError):
    description = "Could not establish connection to the database"


class NotEnoughUniqueFacesError(BadRequest):
    description = "Not enough unique faces to start training a new classifier model. " \
                  "Deleting existing classifiers, if any."
