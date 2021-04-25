package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.dto.FacesDetectionResponseDto;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("detectionService")
@RequiredArgsConstructor
public class FaceDetectionProcessServiceImpl implements FaceProcessService {

    private final FacesApiClient facesApiClient;
    private final ImageExtensionValidator imageExtensionValidator;
    private final FacesMapper facesMapper;

    @Override
    public FacesDetectionResponseDto processImage(ProcessImageParams processImageParams) {
        MultipartFile file = (MultipartFile) processImageParams.getFile();
        imageExtensionValidator.validate(file);
        Integer limit = processImageParams.getLimit();
        Double detProbThreshold = processImageParams.getDetProbThreshold();
        String facePlugins = processImageParams.getFacePlugins();
        FindFacesResponse findFacesResponse = facesApiClient.findFaces(file, limit, detProbThreshold, facePlugins);
        FacesDetectionResponseDto facesDetectionResponseDto = facesMapper.toFacesDetectionResponseDto(findFacesResponse);
        return facesDetectionResponseDto.prepareResponse(processImageParams);
    }
}
