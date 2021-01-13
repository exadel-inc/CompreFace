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

package com.exadel.frs.core.trainservice.sdk.faces.feign;

import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FacesStatusResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.ScanFacesResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.web.multipart.MultipartFile;

public interface FacesFeignClient {

    @RequestLine("POST /scan_faces")
    @Headers("Content-Type: multipart/form-data")
    ScanFacesResponse scanFaces(
            @Param(value = "file")
                    MultipartFile photo,
            @Param(value = "limit")
                    Integer faceLimit,
            @Param(value = "det_prob_threshold")
                    Double thresholdC);

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

    @RequestLine("GET /status")
    @Headers("Content-Type: multipart/form-data")
    FacesStatusResponse getStatus();
}