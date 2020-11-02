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

package com.exadel.frs.core.trainservice.system.feign.python;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.web.multipart.MultipartFile;

public interface FacesClient {

    @RequestLine("POST /scan_faces")
    @Headers("Content-Type: multipart/form-data")
    ScanResponse scanFaces(@Param(value = "file") MultipartFile photo,
                           @Param(value = "limit") Integer faceLimit,
                           @Param(value = "det_prob_threshold") Double thresholdC);

    @RequestLine("GET /status")
    @Headers("Content-Type: multipart/form-data")
    StatusResponse getStatus();
}