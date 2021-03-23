package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.commonservice.exception.TooManyFacesException;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.dto.FaceMatch;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.dto.VerifyFacesResponse;
import com.exadel.frs.core.trainservice.dto.VerifyFacesResultDto;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.exception.NoFacesFoundException;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        List<FindFacesResponse> findFacesResults = getFaceResult(processImageParams);
        VerifyFacesResponse result = getResult(findFacesResults.get(0), findFacesResults.get(1));
        return result.prepareResponse(processImageParams);
    }

    private List<FindFacesResponse> getFaceResult(ProcessImageParams processImageParams) {
        Map<String, MultipartFile> fileMap = (Map<String, MultipartFile>) processImageParams.getFile();
        MultipartFile processFile = fileMap.get("processFile");
        MultipartFile checkFile = fileMap.get("checkFile");
        imageValidator.validate(processFile);
        imageValidator.validate(checkFile);
        if (processImageParams.getDetProbThreshold() == null && StringUtils.isEmpty(processImageParams.getFacePlugins())) {
            processImageParams.setFacePlugins(null);
        }

        FindFacesResponse processFileResponse = client.findFacesWithCalculator(processFile, processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());
        if (processFileResponse == null || CollectionUtils.isEmpty(processFileResponse.getResult())) {
            throw new NoFacesFoundException();
        } else if (processFileResponse.getResult().size() > 1) {
            throw new TooManyFacesException();
        }

        FindFacesResponse checkFileResponse = client.findFacesWithCalculator(checkFile, processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());
        return List.of(processFileResponse, checkFileResponse);
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
                checkFileResult.getResult().stream().map(r -> getFaceMatch(r, classifierPredictor.verify(
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
