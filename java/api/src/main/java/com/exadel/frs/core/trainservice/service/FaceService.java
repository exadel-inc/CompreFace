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

import com.exadel.frs.commonservice.entity.Face;
import com.exadel.frs.commonservice.exception.TooManyFacesException;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.core.trainservice.cache.FaceBO;
import com.exadel.frs.core.trainservice.cache.FaceCacheProvider;
import com.exadel.frs.core.trainservice.cache.FaceCollection;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.component.classifiers.EuclideanDistanceClassifier;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dto.FaceResponseDto;
import com.exadel.frs.core.trainservice.dto.FaceVerification;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.dto.VerificationResult;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.RoundingMode.HALF_UP;
import static java.util.stream.Collectors.toSet;

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

    public void deleteFacesByModel(final String modelKey) {
        faceDao.deleteFacesByApiKey(modelKey);
        faceCacheProvider.invalidate(modelKey);
    }

    public int countFacesInModel(final String modelKey) {
        return faceCacheProvider.getOrLoad(modelKey).getFaces().size();
    }

    public FaceResponseDto findAndSaveFace(
            final String base64photo,
            final String faceName,
            final Double detProbThreshold,
            final String modelKey
    ) throws IOException {
        FindFacesResponse findFacesResponse = facesApiClient.findFacesBase64WithCalculator(
                base64photo,
                MAX_FACES_TO_RECOGNIZE,
                detProbThreshold,
                null
        );

        return findAndSaveFace(Base64.getDecoder().decode(base64photo), faceName, modelKey, findFacesResponse);
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

        return findAndSaveFace(file.getBytes(), faceName, modelKey, findFacesResponse);
    }

    private FaceResponseDto findAndSaveFace(byte[] content,
                                            String faceName,
                                            String modelKey,
                                            FindFacesResponse findFacesResponse) throws IOException {
        List<FindFacesResult> result = findFacesResponse.getResult();

        if (result.size() > MAX_FACES_TO_SAVE) {
            throw new TooManyFacesException();
        }

        Double[] embedding = result.stream()
                .findFirst().orElseThrow()
                .getEmbedding();

        double[] normalized = classifier.normalizeOne(Arrays.stream(embedding).mapToDouble(d -> d).toArray());

        List<Double> normalizedList = Arrays.stream(normalized).boxed().collect(Collectors.toList());

        var embeddingToSave = new Face.Embedding(
                normalizedList,
                findFacesResponse.getPluginsVersions().getCalculator()
        );

        FaceBO faceBO = faceCacheProvider
                .getOrLoad(modelKey)
                .addFace(faceDao.addNewFace(embeddingToSave, content, faceName, modelKey));

        return Optional.ofNullable(faceMapper.toResponseDto(faceBO))
                .orElse(new FaceResponseDto());
    }

    public VerificationResult verifyFace(ProcessImageParams processImageParams) {
        FindFacesResponse findFacesResponse;
        if (processImageParams.getFile() != null) {
            MultipartFile file = (MultipartFile) processImageParams.getFile();
            findFacesResponse = client.findFacesWithCalculator(file, processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());
        } else {
            findFacesResponse = client.findFacesBase64WithCalculator(processImageParams.getImageBase64(), processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());
        }

        if (findFacesResponse == null) {
            return new VerificationResult(Collections.emptyList());
        }

        var results = new ArrayList<FaceVerification>();
        FaceCollection orLoad = faceCacheProvider.getOrLoad(processImageParams.getApiKey());

        for (val findResult : findFacesResponse.getResult()) {
            val prediction = classifierPredictor.verify(
                    processImageParams.getApiKey(),
                    Stream.of(findResult.getEmbedding())
                            .mapToDouble(d -> d)
                            .toArray(),
                    String.valueOf(processImageParams.getAdditionalParams().get("image_id"))
            );

            val inBoxProb = BigDecimal
                    .valueOf(findResult.getBox().getProbability())
                    .setScale(5, HALF_UP)
                    .doubleValue();
            findResult.getBox().setProbability(inBoxProb);

            val pred = BigDecimal
                    .valueOf(prediction)
                    .setScale(5, HALF_UP)
                    .floatValue();

            FaceVerification faceVerification = FaceVerification
                    .builder()
                    .box(findResult.getBox())
                    .similarity(pred)
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

        var result = new VerificationResult(results);
        if (Boolean.TRUE.equals(processImageParams.getStatus())) {
            result.setPluginsVersions(faceMapper.toPluginVersionsDto(findFacesResponse.getPluginsVersions()));
        }

        return result;
    }

}