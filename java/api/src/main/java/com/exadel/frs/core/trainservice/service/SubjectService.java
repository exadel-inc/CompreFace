package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.exception.TooManyFacesException;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.core.trainservice.cache.EmbeddingCacheProvider;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.component.classifiers.EuclideanDistanceClassifier;
import com.exadel.frs.core.trainservice.dao.SubjectDao;
import com.exadel.frs.core.trainservice.dto.EmbeddingInfo;
import com.exadel.frs.core.trainservice.dto.FaceVerification;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.system.global.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
    private final FacesMapper facesMapper;
    private final FacesApiClient facesApiClient;
    private final EmbeddingCacheProvider embeddingCacheProvider;
    private final FaceClassifierPredictor predictor;
    private final EuclideanDistanceClassifier classifier;

    public Collection<String> getSubjectsNames(final String apiKey) {
        return subjectDao.getSubjectNames(apiKey);
    }

    public Subject createSubject(final String apiKey, final String subjectName) {
        // subject is empty (without embeddings) no need to update cache
        return subjectDao.createSubject(apiKey, subjectName);
    }

    public int deleteSubjectsByApiKey(final String apiKey) {
        int deletedCount = subjectDao.deleteSubjectsByApiKey(apiKey);
        // we need invalidate cache
        embeddingCacheProvider.invalidate(apiKey);

        return deletedCount;
    }

    public int removeAllSubjectEmbeddings(final String apiKey, final String subjectName) {
        int removed = subjectDao.removeAllSubjectEmbeddings(apiKey, subjectName);
        if (removed > 0) {
            embeddingCacheProvider.ifPresent(
                    apiKey,
                    c -> c.removeEmbeddingsBySubjectName(subjectName)
            );
        }

        return removed;
    }

    public Subject deleteSubjectByName(final String apiKey, final String subjectName) {
        var subject = subjectDao.deleteSubjectByName(apiKey, subjectName);

        // remove subject from cache if required
        embeddingCacheProvider.ifPresent(
                apiKey,
                c -> c.removeEmbeddingsBySubjectName(subjectName)
        );

        return subject;
    }

    public Embedding removeSubjectEmbedding(final String apiKey, final UUID embeddingId) {
        var embedding = subjectDao.removeSubjectEmbedding(apiKey, embeddingId);

        // remove embedding from cache if required
        embeddingCacheProvider.ifPresent(
                apiKey,
                c -> c.removeEmbedding(embedding)
        );

        return embedding;
    }

    public boolean updateSubjectName(final String apiKey, final String oldSubjectName, final String newSubjectName) {
        if (StringUtils.isEmpty(newSubjectName) || newSubjectName.equals(oldSubjectName)) {
            // no need to update with empty or similar name
            return false;
        }

        boolean updated = subjectDao.updateSubjectName(apiKey, oldSubjectName, newSubjectName);

        if (updated) {
            // update cache if required
            embeddingCacheProvider.ifPresent(
                    apiKey,
                    c -> c.updateSubjectName(oldSubjectName, newSubjectName)
            );
        }

        return updated;
    }

    public Pair<Subject, Embedding> saveCalculatedEmbedding(
            final String base64photo,
            final String subjectName,
            final Double detProbThreshold,
            final String modelKey) {
        var findFacesResponse = facesApiClient.findFacesBase64WithCalculator(
                base64photo,
                MAX_FACES_TO_RECOGNIZE,
                detProbThreshold,
                null
        );

        return saveCalculatedEmbedding(
                Base64.getDecoder().decode(base64photo),
                subjectName,
                modelKey,
                findFacesResponse
        );
    }

    public Pair<Subject, Embedding> saveCalculatedEmbedding(
            final MultipartFile file,
            final String subjectName,
            final Double detProbThreshold,
            final String modelKey
    ) throws IOException {
        var findFacesResponse = facesApiClient.findFacesWithCalculator(
                file,
                MAX_FACES_TO_RECOGNIZE,
                detProbThreshold,
                null
        );

        return saveCalculatedEmbedding(
                file.getBytes(),
                subjectName,
                modelKey,
                findFacesResponse
        );
    }

    private Pair<Subject, Embedding> saveCalculatedEmbedding(byte[] content,
                                                             String subjectName,
                                                             String modelKey,
                                                             FindFacesResponse findFacesResponse) {

        // if we are here => at least one face exists
        List<FindFacesResult> result = findFacesResponse.getResult();

        if (result.size() > MAX_FACES_TO_SAVE) {
            throw new TooManyFacesException();
        }

        Double[] embedding = result.stream().findFirst().orElseThrow().getEmbedding();
        double[] normalized = classifier.normalizeOne(Arrays.stream(embedding).mapToDouble(d -> d).toArray());

        var embeddingToSave = new EmbeddingInfo(
                findFacesResponse.getPluginsVersions().getCalculator(),
                normalized,
                content
        );

        final Pair<Subject, Embedding> pair = subjectDao.addEmbedding(modelKey, subjectName, embeddingToSave);

        embeddingCacheProvider.ifPresent(
                modelKey,
                subjectCollection -> subjectCollection.addEmbedding(pair.getRight())
        );

        return pair;
    }

    public List<FaceVerification> verifyFace(ProcessImageParams processImageParams) {
        FindFacesResponse findFacesResponse;
        if (processImageParams.getFile() != null) {
            MultipartFile file = (MultipartFile) processImageParams.getFile();
            findFacesResponse = facesApiClient.findFacesWithCalculator(file, processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());
        } else {
            findFacesResponse = facesApiClient.findFacesBase64WithCalculator(processImageParams.getImageBase64(), processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());
        }

        if (findFacesResponse == null) {
            return Collections.emptyList();
        }

        var embeddingId = (UUID) processImageParams.getAdditionalParams().get(Constants.IMAGE_ID);

        final String subjectName = embeddingCacheProvider
                .getOrLoad(processImageParams.getApiKey()) // do we really need to load cache here?
                .getSubjectNameByEmbeddingId(embeddingId)
                .orElse("");

        var pluginsVersionsDto = facesMapper.toPluginVersionsDto(findFacesResponse.getPluginsVersions());
        var results = new ArrayList<FaceVerification>();
        for (var findResult : findFacesResponse.getResult()) {
            var prediction = predictor.verify(
                    processImageParams.getApiKey(),
                    Stream.of(findResult.getEmbedding()).mapToDouble(d -> d).toArray(),
                    embeddingId
            );

            var inBoxProb = BigDecimal.valueOf(findResult.getBox().getProbability());
            inBoxProb = inBoxProb.setScale(5, HALF_UP);
            findResult.getBox().setProbability(inBoxProb.doubleValue());

            var pred = BigDecimal.valueOf(prediction);
            pred = pred.setScale(5, HALF_UP);

            var faceVerification = FaceVerification
                    .builder()
                    .box(findResult.getBox())
                    .subject(subjectName)
                    .similarity(pred.floatValue())
                    .landmarks(findResult.getLandmarks())
                    .age(findResult.getAge())
                    .gender(findResult.getGender())
                    .embedding(findResult.getEmbedding())
                    .executionTime(findResult.getExecutionTime())
                    .pluginsVersions(pluginsVersionsDto)
                    .build()
                    .prepareResponse(processImageParams); // do some tricks with obj

            results.add(faceVerification);
        }

        return results;
    }
}
