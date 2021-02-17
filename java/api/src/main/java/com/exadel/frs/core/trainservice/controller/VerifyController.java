package com.exadel.frs.core.trainservice.controller;


import com.exadel.frs.commonservice.exception.TooManyFacesException;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.dto.VerifyFacesResponse;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.exception.NoFacesFoundException;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static java.math.RoundingMode.HALF_UP;

@RestController
@RequestMapping(API_V1)
@RequiredArgsConstructor
@Validated
public class VerifyController {

    private final FaceClassifierPredictor classifierPredictor;
    private final FacesApiClient client;
    private final ImageExtensionValidator imageValidator;
    private final FacesMapper mapper;

    @PostMapping(value = "/verify")
    public Map<String, List<VerifyFacesResponse>> verify(
            @ApiParam(value = "Api key of application and model", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = "File to be verified", required = true)
            @RequestParam final MultipartFile processFile,
            @ApiParam(value = "Reference file to check the processed file", required = true)
            @RequestParam final MultipartFile checkFile,
            @ApiParam(value = "Maximum number of faces to be verified")
            @RequestParam(defaultValue = "0", required = false)
            @Min(value = 0, message = "Limit should be equal or greater than 0") final Integer limit,
            @ApiParam(value = "The minimal percent confidence that found face is actually a face.")
            @RequestParam(value = "det_prob_threshold", required = false) final Double detProbThreshold,
            @ApiParam(value = "Comma-separated types of face plugins. Empty value - face plugins disabled, returns only bounding boxes")
            @RequestParam(value = "face_plugins", required = false)
            final String facePlugins
    ) {
        // find FaceResult for each file
        List<FindFacesResult> findFacesResults = Stream.of(processFile, checkFile)
                .parallel()
                .map(file -> getFaceResult(file, limit, detProbThreshold, facePlugins))
                .collect(Collectors.toList());

        return Map.of("result", Collections.singletonList(
                getResult(findFacesResults.get(0), findFacesResults.get(1)))
        );
    }

    private FindFacesResult getFaceResult(MultipartFile file, int limit, Double detProbThreshold, String facePlugins) {
        imageValidator.validate(file);
        FindFacesResponse findFacesResponse = client.findFacesWithCalculator(file, limit, detProbThreshold, facePlugins);

        if (CollectionUtils.isEmpty(findFacesResponse.getResult())) {
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
