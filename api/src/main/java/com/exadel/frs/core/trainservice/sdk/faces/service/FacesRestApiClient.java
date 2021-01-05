package com.exadel.frs.core.trainservice.sdk.faces.service;

import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.exception.FacesServiceException;
import com.exadel.frs.core.trainservice.sdk.faces.exception.NoFacesFoundException;
import com.exadel.frs.core.trainservice.sdk.faces.feign.FacesFeignClient;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.ScanResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.StatusResponse;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class FacesRestApiClient implements FacesApiClient {

    private final FacesFeignClient feignClient;

    @Override
    public ScanResponse scanFaces(final MultipartFile photo, final Integer faceLimit, final Double thresholdC) {
        try {
            return feignClient.scanFaces(photo, faceLimit, thresholdC);
        } catch (FeignException.BadRequest ex) {
            throw new NoFacesFoundException();
        } catch (FeignException e) {
            throw new FacesServiceException(e.getMessage());
        }
    }

    @Override
    public FindFacesResponse findFaces(final MultipartFile photo, final Integer faceLimit, final Double thresholdC, final String facePlugins) {
        try {
            return feignClient.findFaces(photo, faceLimit, thresholdC, facePlugins);
        } catch (FeignException.BadRequest ex) {
            throw new NoFacesFoundException();
        } catch (FeignException e) {
            throw new FacesServiceException(e.getMessage());
        }
    }

    @Override
    public StatusResponse getStatus() {
        try {
            return feignClient.getStatus();
        } catch (FeignException e) {
            throw new FacesServiceException(e.getMessage());
        }
    }
}
