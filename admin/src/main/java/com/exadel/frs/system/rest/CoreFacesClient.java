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

package com.exadel.frs.system.rest;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface CoreFacesClient {

    @RequestLine("DELETE /api/v1/faces")
    @Headers("x-frs-api-key: {apiKey}")
    int deleteFaces(@Param("apiKey") final String apiKey);

    @RequestLine("PUT /api/v1/models/api-key?new_model_api_key={newModelKey}")
    @Headers("x-frs-api-key: {apiKey}")
    void updateModelKeyForFaces(
            @Param("apiKey") final String apiKey,
            @Param("newModelKey") final String newModelKey
    );
}