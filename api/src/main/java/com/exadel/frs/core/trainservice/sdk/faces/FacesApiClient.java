package com.exadel.frs.core.trainservice.sdk.faces;

import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FacesStatusResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface representing Client for Faces API.
 */
public interface FacesApiClient {

    FindFacesResponse findFacesWithCalculator(
            MultipartFile photo,
            Integer faceLimit,
            Double thresholdC,
            String facePlugins);

    FindFacesResponse findFaces(
            MultipartFile photo,
            Integer faceLimit,
            Double thresholdC,
            String facePlugins);

    FacesStatusResponse getStatus();
}
