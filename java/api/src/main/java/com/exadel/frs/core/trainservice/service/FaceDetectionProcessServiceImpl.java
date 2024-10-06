package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.dto.FacesDetectionResponseDto;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
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
        Integer limit = processImageParams.getLimit();
        Double detProbThreshold = processImageParams.getDetProbThreshold();
        String facePlugins = processImageParams.getFacePlugins();

        FindFacesResponse findFacesResponse;
        if (processImageParams.getFile() != null) {
            MultipartFile file = (MultipartFile) processImageParams.getFile();
            imageExtensionValidator.validate(file);
            findFacesResponse = facesApiClient.findFaces(file, limit, detProbThreshold, facePlugins, true);
        } else {
            imageExtensionValidator.validateBase64(processImageParams.getImageBase64());
            findFacesResponse = facesApiClient.findFacesBase64(processImageParams.getImageBase64(), limit, detProbThreshold, facePlugins,true);
        }

        FacesDetectionResponseDto facesDetectionResponseDto = facesMapper.toFacesDetectionResponseDto(findFacesResponse);
        return facesDetectionResponseDto.prepareResponse(processImageParams);
    }
}
