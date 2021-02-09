package com.exadel.frs.core.trainservice.sdk.faces.service;

import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.exception.FacesServiceException;
import com.exadel.frs.core.trainservice.sdk.faces.exception.NoFacesFoundException;
import com.exadel.frs.core.trainservice.sdk.faces.feign.FacesFeignClient;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FacesStatusResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class FacesRestApiClient implements FacesApiClient {

    public static final String CALCULATOR_PLUGIN = "calculator";
    private static final String COMMA = ",";

    private final FacesFeignClient feignClient;

    @Override
    public FindFacesResponse findFaces(final MultipartFile photo, final Integer faceLimit, final Double thresholdC,
                                       final String facePlugins) {
        try {
            return feignClient.findFaces(photo, faceLimit, thresholdC, facePlugins);
        } catch (FeignException.BadRequest ex) {
            throw new NoFacesFoundException();
        } catch (FeignException e) {
            throw new FacesServiceException(e.getMessage());
        }
    }

    @Override
    public FindFacesResponse findFacesWithCalculator(final MultipartFile photo, final Integer faceLimit, final Double thresholdC,
                                                     final String facePlugins) {
        try {
            String finalFacePlugins;
            if (StringUtils.isNotBlank(facePlugins)) {
                if (!facePlugins.contains(CALCULATOR_PLUGIN)) {
                    finalFacePlugins = CALCULATOR_PLUGIN + COMMA + facePlugins;
                } else {
                    finalFacePlugins = facePlugins;
                }
            } else {
                finalFacePlugins = CALCULATOR_PLUGIN;
            }

            return feignClient.findFaces(photo, faceLimit, thresholdC, finalFacePlugins);
        } catch (FeignException.BadRequest ex) {
            throw new NoFacesFoundException();
        } catch (FeignException e) {
            throw new FacesServiceException(e.getMessage());
        }
    }

    @Override
    public FacesStatusResponse getStatus() {
        try {
            return feignClient.getStatus();
        } catch (FeignException e) {
            throw new FacesServiceException(e.getMessage());
        }
    }
}
