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

package com.exadel.frs.commonservice.sdk.faces.feign;

import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesStatusResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesRequest;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public interface FacesFeignClient {

    @RequestLine("POST /find_faces")
    @Headers("Content-Type: multipart/form-data")
    FindFacesResponse findFaces(
            @Param(value = "file")
                    MultipartFile photo,
            @Param(value = "limit")
                    Integer faceLimit,
            @Param(value = "det_prob_threshold")
                    Double thresholdC,
            @Param(value = "face_plugins")
                    String facePlugins);

    @RequestLine("POST /find_faces_base64?limit={limit}&det_prob_threshold={threshold}&face_plugins={plugins}")
    @Headers("Content-Type: " + MediaType.APPLICATION_JSON_VALUE)
    FindFacesResponse findFacesBase64(
            FindFacesRequest request,
            @Param(value = "limit") Integer faceLimit,
            @Param(value = "threshold") Double thresholdC,
            @Param(value = "plugins") String facePlugins);

    @RequestLine("GET /status")
    @Headers("Content-Type: multipart/form-data")
    FacesStatusResponse getStatus();
}
