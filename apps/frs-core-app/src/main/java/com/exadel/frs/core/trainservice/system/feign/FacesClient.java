package com.exadel.frs.core.trainservice.system.feign;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.web.multipart.MultipartFile;

public interface FacesClient {

    @RequestLine("POST /scan_faces")
    @Headers("Content-Type: multipart/form-data")
    ScanResponse scanFaces(@Param(value = "file") MultipartFile photo,
                           @Param(value = "limit") Integer faceLimit,
                           @Param(value = "threshold_c") Double thresholdC);

    @RequestLine("GET /status")
    @Headers("Content-Type: multipart/form-data")
    StatusResponse getStatus();
}