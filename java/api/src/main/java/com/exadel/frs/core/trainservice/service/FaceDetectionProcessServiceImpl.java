package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.dto.FacesDetectionResponseDto;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("detectionService")
@RequiredArgsConstructor
public class FaceDetectionProcessServiceImpl implements FaceProcessService {

    private final FacesApiClient client;
    private final ImageExtensionValidator imageValidator;
    private final FacesMapper mapper;

    @Override
    public FacesDetectionResponseDto processImage(ProcessImageParams processImageParams) {
        MultipartFile file = (MultipartFile) processImageParams.getFile();
        imageValidator.validate(file);
        client.getStatus();
        FacesDetectionResponseDto facesDetectionResponseDto = mapper.toFacesDetectionResponseDto(
                client.findFaces(file, processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins()));
        return facesDetectionResponseDto.prepareResponse(facesDetectionResponseDto, processImageParams);
    }
}
