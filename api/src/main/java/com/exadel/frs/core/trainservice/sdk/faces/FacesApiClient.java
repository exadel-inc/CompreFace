package com.exadel.frs.core.trainservice.sdk.faces;

import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FacesStatusResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.ScanFacesResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface representing Client for Faces API.
 */
public interface FacesApiClient {

    ScanFacesResponse scanFaces(
            MultipartFile photo,
            Integer faceLimit,
            Double thresholdC);

    FindFacesResponse findFaces(
            MultipartFile photo,
            Integer faceLimit,
            Double thresholdC,
            String facePlugins);

    FacesStatusResponse getStatus();
}
