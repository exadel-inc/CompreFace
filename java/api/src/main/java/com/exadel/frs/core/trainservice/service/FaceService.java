/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.core.trainservice.service;

import static java.math.RoundingMode.HALF_UP;
import static java.util.stream.Collectors.toSet;
import com.exadel.frs.commonservice.entity.Face;
import com.exadel.frs.commonservice.exception.FaceNotFoundException;
import com.exadel.frs.commonservice.exception.TooManyFacesException;
import com.exadel.frs.core.trainservice.cache.FaceBO;
import com.exadel.frs.core.trainservice.cache.FaceCacheProvider;
import com.exadel.frs.core.trainservice.cache.FaceCollection;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.component.classifiers.EuclideanDistanceClassifier;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dto.FaceResponseDto;
import com.exadel.frs.core.trainservice.dto.FaceVerification;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResult;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaceService {

    private static final int MAX_FACES_TO_SAVE = 1;
    public static final int MAX_FACES_TO_RECOGNIZE = 2;

    private final FacesApiClient facesApiClient;
    private final FaceDao faceDao;
    private final FaceCacheProvider faceCacheProvider;
    private final FaceClassifierPredictor classifierPredictor;
    private final FacesApiClient client;
    private final FacesMapper faceMapper;
    private final EuclideanDistanceClassifier classifier;

    public List<FaceResponseDto> findFaces(final String apiKey) {
        Set<FaceBO> faces = faceCacheProvider.getOrLoad(apiKey).getFaces();
        return faceMapper.toResponseDto(faces);
    }

    public List<FaceResponseDto> deleteFaceByName(final String faceName, final String apiKey) {
        val faces = faceCacheProvider.getOrLoad(apiKey);

        Set<FaceBO> collect = faceDao.deleteFaceByName(faceName, apiKey)
                                     .stream()
                                     .map(face -> faces.removeFace(face.getId(), face.getFaceName()))
                                     .collect(toSet());
        log.info("faceMapper: {}", faceMapper);

        return faceMapper.toResponseDto(collect);
    }

    public FaceResponseDto deleteFaceById(final String id, final String apiKey) {
        val collection = faceCacheProvider.getOrLoad(apiKey);
        val face = faceDao.deleteFaceById(id);
        if (face != null) {
            FaceBO faceBO = collection.removeFace(face.getId(), face.getFaceName());
            return faceMapper.toResponseDto(faceBO);
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
            throw new FaceNotFoundException(oldSubject);
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
        FindFacesResponse findFacesResponse = facesApiClient.findFacesWithCalculator(file, MAX_FACES_TO_RECOGNIZE, detProbThreshold, null);
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
        FaceResponseDto faceResponseDto = faceMapper.toResponseDto(faceBO);
        if (faceResponseDto == null) {
            faceResponseDto = new FaceResponseDto();
        }

        return faceResponseDto;
    }

    public Map<String, List<FaceVerification>> verifyFace(ProcessImageParams processImageParams) {
        MultipartFile file = (MultipartFile) processImageParams.getFile();

        FindFacesResponse findFacesResponse = client.findFacesWithCalculator(file, processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());
        if (findFacesResponse == null) {
            return Map.of("result", Collections.emptyList());
        }

        val results = new ArrayList<FaceVerification>();
        FaceCollection orLoad = faceCacheProvider.getOrLoad(processImageParams.getApiKey());
        for (val findResult : findFacesResponse.getResult()) {
            val prediction = classifierPredictor.verify(
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

        if (orLoad != null && orLoad.getFacesMap() != null && orLoad.getFacesMap().inverse() != null && orLoad.getFacesMap().inverse().get(0) != null) {
            results.forEach(r -> r.setSubject(orLoad.getFacesMap().inverse().get(0).getName()));
        }

        return Map.of("result", results);
    }
}