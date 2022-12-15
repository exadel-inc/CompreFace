package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.commonservice.exception.IncorrectPredictionCountException;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.dto.FacePredictionResultDto;
import com.exadel.frs.core.trainservice.dto.FaceSimilarityDto;
import com.exadel.frs.core.trainservice.dto.FacesRecognitionResponseDto;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Stream;

import static com.exadel.frs.core.trainservice.system.global.Constants.PREDICTION_COUNT;
import static java.math.RoundingMode.HALF_UP;

@Slf4j
@RequiredArgsConstructor
public class FaceRecognizeProcessServiceImpl implements FaceProcessService {

    private final FaceClassifierPredictor classifierPredictor;
    private final FacesApiClient facesApiClient;
    private final ImageExtensionValidator imageExtensionValidator;
    private final FacesMapper facesMapper;

    @Override
    public FacesRecognitionResponseDto processImage(ProcessImageParams processImageParams) {
        Object predictionCountObj = processImageParams.getAdditionalParams().get(PREDICTION_COUNT);
        Integer predictionCount = (Integer) predictionCountObj;
        if (predictionCount == 0 || predictionCount < -1) {
            throw new IncorrectPredictionCountException();
        }

        FindFacesResponse findFacesResponse;

        if (processImageParams.getFile() != null) {
            MultipartFile file = (MultipartFile) processImageParams.getFile();
            imageExtensionValidator.validate(file);
            findFacesResponse = facesApiClient.findFacesWithCalculator(
                    file,
                    processImageParams.getLimit(),
                    processImageParams.getDetProbThreshold(),
                    processImageParams.getFacePlugins(),
                    processImageParams.getDetectFaces()
            );
        } else {
            imageExtensionValidator.validateBase64(processImageParams.getImageBase64());
            findFacesResponse = facesApiClient.findFacesBase64WithCalculator(
                    processImageParams.getImageBase64(),
                    processImageParams.getLimit(),
                    processImageParams.getDetProbThreshold(),
                    processImageParams.getFacePlugins(),
                    processImageParams.getDetectFaces()
            );
        }

        val facesRecognitionDto = facesMapper.toFacesRecognitionResponseDto(findFacesResponse);
        if (facesRecognitionDto == null) {
            return FacesRecognitionResponseDto.builder().build();
        }

        String apiKey = processImageParams.getApiKey();
        for (val findResult : facesRecognitionDto.getResult()) {
            final ArrayList<FaceSimilarityDto> faces = processFaceResult(predictionCount, apiKey, findResult);

            findResult.setSubjects(faces);
        }

        return facesRecognitionDto.prepareResponse(processImageParams);
    }

    private ArrayList<FaceSimilarityDto> processFaceResult(Integer predictionCount, String apiKey, FacePredictionResultDto findResult) {
        double[] input = Stream.of(findResult.getEmbedding()).mapToDouble(d -> d).toArray();
        val predictions = classifierPredictor.predict(apiKey, input, predictionCount);
        val faces = new ArrayList<FaceSimilarityDto>();
        for (val prediction : predictions) {
            var pred = BigDecimal.valueOf(prediction.getLeft());
            pred = pred.setScale(5, HALF_UP);
            faces.add(new FaceSimilarityDto(prediction.getRight(), pred.floatValue()));
        }

        var inBoxProb = BigDecimal.valueOf(findResult.getBox().getProbability());
        inBoxProb = inBoxProb.setScale(5, HALF_UP);
        findResult.getBox().setProbability(inBoxProb.doubleValue());
        return faces;
    }
}
