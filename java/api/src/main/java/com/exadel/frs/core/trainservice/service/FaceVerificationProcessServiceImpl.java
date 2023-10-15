package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.commonservice.exception.TooManyFacesException;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.exception.NoFacesFoundException;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesBox;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.dto.FaceMatch;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.dto.VerifyFacesResponse;
import com.exadel.frs.core.trainservice.dto.VerifyFacesResultDto;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.exadel.frs.core.trainservice.system.global.Constants.SOURCE_IMAGE;
import static com.exadel.frs.core.trainservice.system.global.Constants.TARGET_IMAGE;
import static java.math.RoundingMode.HALF_UP;

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

    @SuppressWarnings("unchecked")
    private List<FindFacesResponse> getFaceResult(ProcessImageParams processImageParams) {
        Map<String, ?> fileMap = (Map<String, ?>) processImageParams.getFile();
        Object sourceImage = fileMap.get(SOURCE_IMAGE);
        Object targetImage = fileMap.get(TARGET_IMAGE);

        if (processImageParams.getDetProbThreshold() == null && StringUtils.isEmpty(processImageParams.getFacePlugins())) {
            processImageParams.setFacePlugins(null);
        }

        SourceTarget<?> sourceTarget;
        if (isMultipartFile(sourceImage) && isMultipartFile(targetImage)) {
            sourceTarget = new MultipartFileSourceTarget(
                    (MultipartFile) sourceImage,
                    (MultipartFile) targetImage
            );
        } else if (isString(sourceImage) && isString(targetImage)) {
            sourceTarget = new Base64SourceTarget(
                    (String) sourceImage,
                    (String) targetImage
            );
        } else {
            throw new IllegalStateException("Wrong file(s) type in process image params");
        }

        return sourceTarget.findFaces(processImageParams);
    }

    private class MultipartFileSourceTarget extends SourceTarget<MultipartFile> {

        public MultipartFileSourceTarget(MultipartFile source, MultipartFile target) {
            super(source, target);
        }

        @Override
        void validate(MultipartFile photo) {
            imageValidator.validate(photo);
        }

        @Override
        FindFacesResponse findFace(MultipartFile photo, ProcessImageParams processImageParams) {
            return client.findFacesWithCalculator(
                    photo,
                    processImageParams.getLimit(),
                    processImageParams.getDetProbThreshold(),
                    processImageParams.getFacePlugins(),
                    true
            );
        }
    }

    private class Base64SourceTarget extends SourceTarget<String> {

        public Base64SourceTarget(String source, String target) {
            super(source, target);
        }

        @Override
        void validate(String photo) {
            imageValidator.validateBase64(photo);
        }

        @Override
        FindFacesResponse findFace(String photo, ProcessImageParams processImageParams) {
            return client.findFacesBase64WithCalculator(
                    photo,
                    processImageParams.getLimit(),
                    processImageParams.getDetProbThreshold(),
                    processImageParams.getFacePlugins(),
                    true
            );
        }
    }

    @RequiredArgsConstructor
    private abstract static class SourceTarget<T> {

        private final T source;
        private final T target;

        public List<FindFacesResponse> findFaces(ProcessImageParams processImageParams) {
            validate(source);
            validate(target);

            FindFacesResponse sourceImageResponse = findFace(source, processImageParams);

            if (sourceImageResponse == null || CollectionUtils.isEmpty(sourceImageResponse.getResult())) {
                throw new NoFacesFoundException();
            } else if (sourceImageResponse.getResult().size() > 1) {
                throw new TooManyFacesException();
            }

            FindFacesResponse targetImageResponse = findFace(target, processImageParams);

            return List.of(sourceImageResponse, targetImageResponse);
        }

        abstract void validate(T photo);

        abstract FindFacesResponse findFace(T photo, ProcessImageParams processImageParams);
    }

    private VerifyFacesResponse getResult(FindFacesResponse sourceImageResult, FindFacesResponse targetImageResult) {
        // replace original probability value with scaled to (5, HALF_UP)
        FindFacesResult sourceFacesResult = sourceImageResult.getResult().get(0);
        roundProbability(sourceFacesResult.getBox());
        List<FindFacesResult> targetFacesResults = targetImageResult.getResult();
        targetFacesResults.forEach(result -> roundProbability(result.getBox()));

        // Get embeddings
        Function<Double[], double[]> toPrimitiveDouble = source -> Arrays.stream(source).mapToDouble(d -> d).toArray();
        double[] sourceImageEmbedding = toPrimitiveDouble.apply(sourceFacesResult.getEmbedding());
        double[][] targetImageEmbeddings = targetFacesResults.stream()
                .map(FindFacesResult::getEmbedding)
                .map(toPrimitiveDouble)
                .toArray(double[][]::new);

        double[] similarities = classifierPredictor.verify(sourceImageEmbedding, targetImageEmbeddings);

        List<FaceMatch> faceMatches = new ArrayList<>();
        int similarityNumber = 0;
        for (FindFacesResult targetFacesResult : targetFacesResults) {
            double similarity = similarities[similarityNumber];
            FaceMatch faceMatch = getFaceMatch(targetFacesResult, similarity);
            faceMatches.add(faceMatch);
            similarityNumber++;
        }

        // compose new result
        return new VerifyFacesResponse(
                mapper.toVerifyFacesResultDto(sourceFacesResult),
                faceMatches,
                mapper.toPluginVersionsDto(sourceImageResult.getPluginsVersions())
        );
    }

    private void roundProbability(FacesBox facesBox) {
        double rounded = BigDecimal
                .valueOf(facesBox.getProbability())
                .setScale(5, HALF_UP).doubleValue();
        facesBox.setProbability(rounded);
    }

    private FaceMatch getFaceMatch(FindFacesResult targetFacesResult, Double similarity) {
        VerifyFacesResultDto verifyFacesResultDto = mapper.toVerifyFacesResultDto(targetFacesResult);
        FaceMatch faceMatch = new FaceMatch();
        faceMatch.setBox(verifyFacesResultDto.getBox());
        faceMatch.setExecutionTime(verifyFacesResultDto.getExecutionTime());
        faceMatch.setEmbedding(verifyFacesResultDto.getEmbedding());
        faceMatch.setAge(verifyFacesResultDto.getAge());
        faceMatch.setGender(verifyFacesResultDto.getGender());
        faceMatch.setPose(verifyFacesResultDto.getPose());
        faceMatch.setLandmarks(verifyFacesResultDto.getLandmarks());
        faceMatch.setSimilarity(BigDecimal.valueOf(similarity).setScale(5, HALF_UP).floatValue());
        faceMatch.setMask(targetFacesResult.getMask());
        return faceMatch;
    }

    private static boolean isMultipartFile(Object input) {
        return input != null && MultipartFile.class.isAssignableFrom(input.getClass());
    }

    private static boolean isString(Object input) {
        return input != null && String.class.isAssignableFrom(input.getClass());
    }
}
