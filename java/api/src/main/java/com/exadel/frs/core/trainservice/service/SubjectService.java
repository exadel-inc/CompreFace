package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.exception.TooManyFacesException;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.core.trainservice.cache.SubjectCacheProvider;
import com.exadel.frs.core.trainservice.cache.SubjectMeta;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.component.classifiers.EuclideanDistanceClassifier;
import com.exadel.frs.core.trainservice.dao.SubjectDao;
import com.exadel.frs.core.trainservice.dto.EmbeddingInfo;
import com.exadel.frs.core.trainservice.dto.FaceVerification;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.system.global.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
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
    private final FaceClassifierPredictor predictor;
    private final EuclideanDistanceClassifier classifier;

    public Collection<SubjectMeta> findSubjects(final String apiKey) {
        return subjectCacheProvider.getOrLoad(apiKey).getMetas();
    }

    public int deleteSubjectsByApiKey(final String apiKey) {
        return subjectDao.deleteSubjectsByApiKey(apiKey);
    }

    public Optional<Subject> deleteSubjectById(final String apiKey, final UUID subjectId) {
        return subjectDao.deleteSubjectById(apiKey, subjectId);
    }

    public Optional<Subject> deleteSubjectByName(final String apiKey, final String subjectName) {
        final Optional<Subject> subjectOptional = subjectDao.deleteSubjectByName(apiKey, subjectName);

        // remove subject from cache if required
        subjectOptional.ifPresent(subject -> subjectCacheProvider.ifPresent(
                apiKey,
                c -> c.removeSubject(subject.getId())
        ));

        return subjectOptional;
    }

    public Optional<Embedding> removeSubjectEmbedding(final String apiKey, final UUID embeddingId) {
        final Optional<Embedding> embeddingOptional = subjectDao.removeSubjectEmbedding(apiKey, embeddingId);

        // remove embedding from cache if required
        embeddingOptional.ifPresent(embedding -> subjectCacheProvider.ifPresent(
                apiKey,
                c -> c.removeEmbedding(embedding)
        ));

        return embeddingOptional;
    }

    public boolean updateSubjectName(final String apiKey, final String oldSubjectName, final String newSubjectName) {
        final Optional<UUID> newSubjectIdOptional = subjectDao.updateSubjectName(apiKey, oldSubjectName, newSubjectName);

        // update cache
        newSubjectIdOptional.ifPresent(newSubjectId -> subjectCacheProvider.ifPresent(
                apiKey,
                c -> c.updateSubjectName(oldSubjectName, newSubjectId, newSubjectName)
        ));

        return newSubjectIdOptional.isPresent();
    }

    public Pair<Subject, Embedding> findAndSaveSubject(
            final MultipartFile file,
            final String subjectName,
            final Double detProbThreshold,
            final String modelKey
    ) throws IOException {
        FindFacesResponse findFacesResponse = facesApiClient.findFacesWithCalculator(
                file,
                MAX_FACES_TO_RECOGNIZE,
                detProbThreshold,
                null
        );

        // if we are here, it means at least one face was found
        List<FindFacesResult> result = findFacesResponse.getResult();

        if (result.size() > MAX_FACES_TO_SAVE) {
            throw new TooManyFacesException();
        }

        Double[] embedding = result.stream().findFirst().orElseThrow().getEmbedding();
        double[] normalized = classifier.normalizeOne(Arrays.stream(embedding).mapToDouble(d -> d).toArray());

        var embeddingToSave = new EmbeddingInfo(
                findFacesResponse.getPluginsVersions().getCalculator(),
                normalized,
                file.getBytes()
        );

        final Pair<Subject, Embedding> pair = subjectDao.addEmbedding(modelKey, subjectName, embeddingToSave);

        subjectCacheProvider.ifPresent(
                modelKey,
                subjectCollection -> subjectCollection.addEmbedding(pair.getRight())
        );

        return pair;
    }

    public Map<String, List<FaceVerification>> verifyFace(ProcessImageParams processImageParams) {
        FindFacesResponse findFacesResponse;
        if (processImageParams.getFile() != null) {
            MultipartFile file = (MultipartFile) processImageParams.getFile();
            findFacesResponse = facesApiClient.findFacesWithCalculator(file, processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());
        } else {
            findFacesResponse = facesApiClient.findFacesBase64WithCalculator(processImageParams.getImageBase64(), processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());
        }

        if (findFacesResponse == null) {
            return Map.of("result", Collections.emptyList());
        }

        var results = new ArrayList<FaceVerification>();

        UUID embeddingId = (UUID) processImageParams.getAdditionalParams().get(Constants.IMAGE_ID);
        final String subject = subjectCacheProvider
                .getOrLoad(processImageParams.getApiKey())
                .getSubjectNameByEmbeddingId(embeddingId)
                .orElse("");

        for (var findResult : findFacesResponse.getResult()) {
            var prediction = predictor.verify(
                    processImageParams.getApiKey(),
                    Stream.of(findResult.getEmbedding()).mapToDouble(d -> d).toArray(),
                    embeddingId.toString()
            );

            var inBoxProb = BigDecimal.valueOf(findResult.getBox().getProbability());
            inBoxProb = inBoxProb.setScale(5, HALF_UP);
            findResult.getBox().setProbability(inBoxProb.doubleValue());

            var pred = BigDecimal.valueOf(prediction);
            pred = pred.setScale(5, HALF_UP);

            var faceVerification = FaceVerification
                    .builder()
                    .box(findResult.getBox())
                    .similarity(pred.floatValue())
                    .embedding(findResult.getEmbedding())
                    .executionTime(findResult.getExecutionTime())
                    .age(findResult.getAge())
                    .gender(findResult.getGender())
                    .landmarks(findResult.getLandmarks())
                    .subject(subject)
                    .build()
                    .prepareResponse(processImageParams); // do some tricks with obj

            results.add(faceVerification);
        }

        return Map.of("result", results);
    }
}
