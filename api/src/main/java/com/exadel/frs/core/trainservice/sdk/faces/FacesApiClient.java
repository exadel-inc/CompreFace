package com.exadel.frs.core.trainservice.sdk.faces;

import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.ScanResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.StatusResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface representing Client for Faces API.
 */
public interface FacesApiClient {

    ScanResponse scanFaces(
            MultipartFile photo,
            Integer faceLimit,
            Double thresholdC);

    FindFacesResponse findFaces(
            MultipartFile photo,
            Integer faceLimit,
            Double thresholdC,
            String facePlugins);

    StatusResponse getStatus();
}
