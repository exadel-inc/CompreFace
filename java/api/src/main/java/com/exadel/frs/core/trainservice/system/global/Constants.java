/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.core.trainservice.system.global;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    public static final String X_FRS_API_KEY_HEADER = "x-api-key";
    public static final String API_V1 = "/api/v1";
    public static final String CALCULATOR_PLUGIN = "calculator";
    public static final String RECOGNIZE = "/recognition/recognize";
    public static final String SOURCE_IMAGE = "source_image";
    public static final String TARGET_IMAGE = "target_image";
    public static final String FACE_PLUGINS = "face_plugins";
    public static final String PREDICTION_COUNT_REQUEST_PARAM = "prediction_count";
    public static final String API_KEY_DESC = "Api key of application and model";
    public static final String IMAGE_FILE_DESC = "Image for recognizing";
    public static final String LIMIT_DESC = "Maximum number of faces to be recognized";
    public static final String LIMIT_MIN_DESC = "Limit should be equal or greater than 0";
    public static final String PREDICTION_COUNT_DESC = "Maximum number of predictions per faces";
    public static final String PREDICTION_COUNT_MIN_DESC = "prediction_count should be equal or greater than 1";
    public static final String DET_PROB_THRESHOLD_DESC = "The minimal percent confidence that found face is actually a face.";
    public static final String FACE_PLUGINS_DESC = "Comma-separated types of face plugins. Empty value - face plugins disabled, returns only bounding boxes";
    public static final String STATUS_DESC = "Special parameter to show execution_time and plugin_version fields. Empty value - both fields eliminated, true - both fields included";
    public static final String DETECT_FACES_DESC = "The parameter specifies whether to perform image detection or not";
    public static final String PREDICTION_COUNT = "predictionCount";
    public static final String STATUS_DEFAULT_VALUE = "false";
    public static final String DETECT_FACES_DEFAULT_VALUE = "true";
    public static final String PREDICTION_COUNT_DEFAULT_VALUE = "1";
    public static final String LIMIT_DEFAULT_VALUE = "0";
    public static final String IMAGE_WITH_ONE_FACE_DESC = "A picture with one face (accepted formats: jpeg, png).";
    public static final String IMAGE_ID_DESC = "Image Id from collection to compare with face.";
    public static final String IMAGE_IDS_DESC = "List of image Ids from collection to compare with face";
    public static final String SUBJECT_DESC = "Person's name to whom the face belongs to.";
    public static final String SUBJECT = "subject";
    public static final String IMAGE_ID = "image_id";
    public static final String SOURCE_IMAGE_DESC = "File to be verified";
    public static final String TARGET_IMAGE_DESC = "Reference file to check the processed file";
    public static final String STATUS = "status";
    public static final String DETECT_FACES = "detect_faces";
    public static final String SUBJECT_NAME_IS_EMPTY = "Subject name is empty";

    public static final String NUMBER_VALUE_EXAMPLE = "1";
    public static final String DEMO_API_KEY = "00000000-0000-0000-0000-000000000002";
    public static final String FACENET2018 = "Facenet2018";
    public static final UUID SERVER_UUID = UUID.randomUUID();
    public static final String CACHE_CONTROL_HEADER_VALUE = "public, max-age=31536000";
}
