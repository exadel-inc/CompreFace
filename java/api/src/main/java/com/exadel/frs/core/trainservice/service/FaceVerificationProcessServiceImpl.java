package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.commonservice.exception.TooManyFacesException;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.dto.FaceMatch;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.dto.VerifyFacesResponse;
import com.exadel.frs.core.trainservice.dto.VerifyFacesResultDto;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.exception.NoFacesFoundException;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.exadel.frs.core.trainservice.system.global.Constants.SOURCE_IMAGE;
import static com.exadel.frs.core.trainservice.system.global.Constants.TARGET_IMAGE;
import static java.math.RoundingMode.HALF_UP;

@Service("verificationService")
@RequiredArgsConstructor
public class FaceVerificationProcessServiceImpl implements FaceProcessService {
    public static final String RESULT = "result";
    private final FaceClassifierPredictor classifierPredictor;
    private final FacesApiClient client;
    private final ImageExtensionValidator imageValidator;
    private final FacesMapper mapper;

    @Override
    public VerifyFacesResponse processImage(ProcessImageParams processImageParams) {
        List<FindFacesResponse> facesResults = getFaceResult(processImageParams);
        VerifyFacesResponse result = getResult(facesResults.get(0), facesResults.get(1));
        return result.prepareResponse(processImageParams);
    }

    private List<FindFacesResponse> getFaceResult(ProcessImageParams processImageParams) {
        Map<String, MultipartFile> fileMap = (Map<String, MultipartFile>) processImageParams.getFile();
        MultipartFile sourceImage = fileMap.get(SOURCE_IMAGE);
        MultipartFile targetImage = fileMap.get(TARGET_IMAGE);
        imageValidator.validate(sourceImage);
        imageValidator.validate(targetImage);
        if (processImageParams.getDetProbThreshold() == null && StringUtils.isEmpty(processImageParams.getFacePlugins())) {
            processImageParams.setFacePlugins(null);
        }

        FindFacesResponse sourceImageResponse = client.findFacesWithCalculator(sourceImage, processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());
        if (sourceImageResponse == null || CollectionUtils.isEmpty(sourceImageResponse.getResult())) {
            throw new NoFacesFoundException();
        } else if (sourceImageResponse.getResult().size() > 1) {
            throw new TooManyFacesException();
        }

        FindFacesResponse targetImageResponse = client.findFacesWithCalculator(targetImage, processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());
        return List.of(sourceImageResponse, targetImageResponse);
    }

    private VerifyFacesResponse getResult(FindFacesResponse processFileResult, FindFacesResponse checkFileResult) {
        // replace original probability value with scaled to (5, HALF_UP)
        FindFacesResult sourceFacesResult = processFileResult.getResult().get(0);
        double inBoxProbDouble = BigDecimal
                .valueOf(sourceFacesResult.getBox().getProbability())
                .setScale(5, HALF_UP).doubleValue();
        FindFacesResult targetFacesResult = checkFileResult.getResult().get(0);
        targetFacesResult.getBox().setProbability(inBoxProbDouble);

        // find prediction
        // To calculate euclidean distance we need one one-ranked array (based on process file) and one two-ranked array
        // (based on check file) that will be used as reference to check processed file
        Function<Double[], double[]> toPrimitiveDouble = source -> Arrays.stream(source).mapToDouble(d -> d).toArray();
        double[] checkFilePrimitiveDouble = toPrimitiveDouble.apply(targetFacesResult.getEmbedding());
        double[][] twoRankedEmbeddings = new double[1][checkFilePrimitiveDouble.length];
        System.arraycopy(checkFilePrimitiveDouble, 0, twoRankedEmbeddings[0], 0, checkFilePrimitiveDouble.length);

        // compose new result
        return new VerifyFacesResponse(
                mapper.toVerifyFacesResultDto(sourceFacesResult),
                processFileResult.getResult().stream().map(r -> getFaceMatch(r, classifierPredictor.verify(
                        toPrimitiveDouble.apply(r.getEmbedding()),
                        twoRankedEmbeddings
                ))).collect(Collectors.toList()),
                mapper.toPluginVersionsDto(processFileResult.getPluginsVersions())
        );
    }

    private FaceMatch getFaceMatch(FindFacesResult targetFacesResult, Double prediction) {
        VerifyFacesResultDto verifyFacesResultDto = mapper.toVerifyFacesResultDto(targetFacesResult);
        FaceMatch faceMatch = new FaceMatch();
        faceMatch.setBox(verifyFacesResultDto.getBox());
        faceMatch.setExecutionTime(verifyFacesResultDto.getExecutionTime());
        faceMatch.setEmbedding(verifyFacesResultDto.getEmbedding());
        faceMatch.setAge(verifyFacesResultDto.getAge());
        faceMatch.setGender(verifyFacesResultDto.getGender());
        faceMatch.setLandmarks(verifyFacesResultDto.getLandmarks());
        faceMatch.setSimilarity(BigDecimal.valueOf(prediction).setScale(5, HALF_UP).floatValue());
        return faceMatch;
    }
}
