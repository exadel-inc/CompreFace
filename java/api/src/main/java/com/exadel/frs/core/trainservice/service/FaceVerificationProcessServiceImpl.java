package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.commonservice.exception.TooManyFacesException;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.dto.VerifyFacesResponse;
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
        Map<String, MultipartFile> fileMap = (Map<String, MultipartFile>) processImageParams.getFile();
        List<FindFacesResult> findFacesResults = Stream.of(fileMap.get("processFile"), fileMap.get("checkFile"))
                .parallel()
                .map(file -> getFaceResult(file, processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins()))
                .collect(Collectors.toList());

        VerifyFacesResponse result = getResult(findFacesResults.get(0), findFacesResults.get(1));

        return result.prepareResponse(processImageParams);
    }

    private FindFacesResult getFaceResult(MultipartFile file, int limit, Double detProbThreshold, String facePlugins) {
        imageValidator.validate(file);
        if (detProbThreshold == null && StringUtils.isEmpty(facePlugins)) {
            facePlugins = null;
        }
        FindFacesResponse findFacesResponse = client.findFacesWithCalculator(file, limit, detProbThreshold, facePlugins);

        if (findFacesResponse == null || CollectionUtils.isEmpty(findFacesResponse.getResult())) {
            throw new NoFacesFoundException();
        } else if (findFacesResponse.getResult().size() > 1) {
            throw new TooManyFacesException();
        }

        return findFacesResponse.getResult().get(0);
    }

    private VerifyFacesResponse getResult(FindFacesResult processFileResult, FindFacesResult checkFileResult) {
        // replace original probability value with scaled to (5, HALF_UP)
        double inBoxProbDouble = BigDecimal
                .valueOf(processFileResult.getBox().getProbability())
                .setScale(5, HALF_UP).doubleValue();
        checkFileResult.getBox().setProbability(inBoxProbDouble);

        // find prediction
        // To calculate euclidean distance we need one one-ranked array (based on process file) and one two-ranked array
        // (based on check file) that will be used as reference to check processed file
        Function<Double[], double[]> toPrimitiveDouble = source -> Arrays.stream(source).mapToDouble(d -> d).toArray();
        double[] checkFilePrimitiveDouble = toPrimitiveDouble.apply(checkFileResult.getEmbedding());
        double[][] twoRankedEmbeddings = new double[1][checkFilePrimitiveDouble.length];
        System.arraycopy(checkFilePrimitiveDouble, 0, twoRankedEmbeddings[0], 0, checkFilePrimitiveDouble.length);

        Double prediction = classifierPredictor.verify(
                toPrimitiveDouble.apply(processFileResult.getEmbedding()),
                twoRankedEmbeddings
        );

        // compose new result
        return new VerifyFacesResponse(
                mapper.toVerifyFacesResultDto(processFileResult),
                mapper.toVerifyFacesResultDto(checkFileResult),
                BigDecimal.valueOf(prediction).setScale(5, HALF_UP).floatValue()
        );
    }
}
