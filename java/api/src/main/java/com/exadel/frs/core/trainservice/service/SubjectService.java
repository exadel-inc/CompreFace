package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.commonservice.entity.Face;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.exception.SubjectNotFoundException;
import com.exadel.frs.commonservice.exception.TooManyFacesException;
import com.exadel.frs.core.trainservice.cache.SubjectCacheProvider;
import com.exadel.frs.core.trainservice.cache.SubjectMeta;
import com.exadel.frs.core.trainservice.cache.FaceBO;
import com.exadel.frs.core.trainservice.cache.FaceCollection;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.component.classifiers.EuclideanDistanceClassifier;
import com.exadel.frs.core.trainservice.dao.SubjectDao;
import com.exadel.frs.core.trainservice.dto.FaceResponseDto;
import com.exadel.frs.core.trainservice.dto.FaceVerification;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.mapper.EmbeddingMapper;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.RoundingMode.HALF_UP;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private static final int MAX_FACES_TO_SAVE = 1;
    public static final int MAX_FACES_TO_RECOGNIZE = 2;

    private final SubjectDao subjectDao;
    private final FacesApiClient facesApiClient;
    private final SubjectCacheProvider subjectCacheProvider;
    private final EmbeddingMapper subjectMapper;
    private final FaceClassifierPredictor predictor;
    private final EuclideanDistanceClassifier classifier;

    public Collection<FaceResponseDto> findSubjectByApiKey(final String apiKey) {
        Set<SubjectMeta> faces = subjectCacheProvider.getOrLoad(apiKey).getMetas();
        return subjectMapper.toResponseDto(faces);
    }

    public Collection<FaceResponseDto> deleteSubjectByName(final String apiKey, final String subjectName) {
        final Optional<Subject> subjectOptional = subjectDao.deleteSubjectByName(apiKey, subjectName);

        if (subjectOptional.isPresent()) {
            final Collection<SubjectMeta> metas = subjectCacheProvider
                    .getOrLoad(apiKey)
                    .removeSubject(subjectOptional.get().getId());

            return subjectMapper.toResponseDto(metas);
        }

        return Collections.emptyList();
    }

    public FaceResponseDto removeSubjectImg(final String apiKey, final UUID imgId) {
        subjectDao.removeSubjectImg(imgId)
        val collection = subjectCacheProvider.getOrLoad(apiKey);
        val face = faceDao.deleteFaceById(embeddingId);
        if (face != null) {
            FaceBO faceBO = collection.removeFace(face.getId(), face.getFaceName());
            return subjectMapper.toResponseDto(faceBO);
        }

        return null;
    }

    public int updateSubject(final String apiKey, final String oldSubject, final String newSubject) {
        Set<FaceBO> faces = faceCacheProvider
                .getOrLoad(apiKey)
                .getFaces();

        boolean oldSubjectExists = faces
                .stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase(oldSubject));

        if (!oldSubjectExists) {
            throw new SubjectNotFoundException(oldSubject);
        }

        int updated = faceDao.updateSubject(apiKey, oldSubject, newSubject);
        log.debug("Updated {} face names {} --> {} for apiKey [{}]", updated, oldSubject, newSubject, apiKey);
        // think about it
        faceCacheProvider.invalidate(apiKey);

        return updated;
    }

    public void deleteFacesByModel(final String modelKey) {
        faceDao.deleteFacesByApiKey(modelKey);
        faceCacheProvider.invalidate(modelKey);
    }

    public int countFacesInModel(final String modelKey) {
        return faceCacheProvider.getOrLoad(modelKey).getFaces().size();
    }

    public FaceResponseDto findAndSaveFace(
            final MultipartFile file,
            final String faceName,
            final Double detProbThreshold,
            final String modelKey
    ) throws IOException {
        FindFacesResponse findFacesResponse = facesApiClient.findFacesWithCalculator(
                file,
                MAX_FACES_TO_RECOGNIZE,
                detProbThreshold,
                null
        );

        List<FindFacesResult> result = findFacesResponse.getResult();

        if (result.size() > MAX_FACES_TO_SAVE) {
            throw new TooManyFacesException();
        }

        Double[] embedding = result.stream()
                .findFirst().orElseThrow()
                .getEmbedding();

        double[] normalized = classifier.normalizeOne(Arrays.stream(embedding).mapToDouble(d -> d).toArray());
        List<Double> normalizedList = Arrays.stream(normalized).boxed().collect(Collectors.toList());

        Face.Embedding embeddingToSave = new Face.Embedding(normalizedList, findFacesResponse.getPluginsVersions().getCalculator());

        FaceBO faceBO = faceCacheProvider
                .getOrLoad(modelKey)
                .addFace(faceDao.addNewFace(embeddingToSave, file, faceName, modelKey));
        FaceResponseDto faceResponseDto = subjectMapper.toResponseDto(faceBO);
        if (faceResponseDto == null) {
            faceResponseDto = new FaceResponseDto();
        }

        return faceResponseDto;
    }

    public Map<String, List<FaceVerification>> verifyFace(ProcessImageParams processImageParams) {
        FindFacesResponse findFacesResponse;
        if (processImageParams.getFile() != null) {
            MultipartFile file = (MultipartFile) processImageParams.getFile();
            findFacesResponse = client.findFacesWithCalculator(file, processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());
        } else {
            findFacesResponse = client.findFacesBase64WithCalculator(processImageParams.getImageBase64(), processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());
        }

        if (findFacesResponse == null) {
            return Map.of("result", Collections.emptyList());
        }

        val results = new ArrayList<FaceVerification>();
        FaceCollection orLoad = faceCacheProvider.getOrLoad(processImageParams.getApiKey());
        for (val findResult : findFacesResponse.getResult()) {
            val prediction = predictor.verify(
                    processImageParams.getApiKey(),
                    Stream.of(findResult.getEmbedding())
                            .mapToDouble(d -> d)
                            .toArray(),
                    String.valueOf(processImageParams.getAdditionalParams().get("image_id"))
            );

            var inBoxProb = BigDecimal.valueOf(findResult.getBox().getProbability());
            inBoxProb = inBoxProb.setScale(5, HALF_UP);
            findResult.getBox().setProbability(inBoxProb.doubleValue());

            var pred = BigDecimal.valueOf(prediction);
            pred = pred.setScale(5, HALF_UP);

            FaceVerification faceVerification = FaceVerification
                    .builder()
                    .box(findResult.getBox())
                    .similarity(pred.floatValue())
                    .embedding(findResult.getEmbedding())
                    .executionTime(findResult.getExecutionTime())
                    .age(findResult.getAge())
                    .gender(findResult.getGender())
                    .landmarks(findResult.getLandmarks())
                    .build();

            results.add(faceVerification.prepareResponse(processImageParams));
        }

        // TODO: WTF?
        if (orLoad != null && orLoad.getFacesMap() != null && orLoad.getFacesMap().inverse() != null && orLoad.getFacesMap().inverse().get(0) != null) {
            results.forEach(r -> r.setSubject(orLoad.getFacesMap().inverse().get(0).getName()));
        }

        return Map.of("result", results);
    }
}
