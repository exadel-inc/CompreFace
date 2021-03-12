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
import com.exadel.frs.core.trainservice.cache.FaceBO;
import com.exadel.frs.core.trainservice.cache.FaceCacheProvider;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dto.FaceResponseDto;
import com.exadel.frs.core.trainservice.dto.FaceVerification;
import com.exadel.frs.core.trainservice.dto.PluginsVersionsDto;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.PluginsVersions;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static java.math.RoundingMode.HALF_UP;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class FaceService {

    private static final int MAX_FACES_TO_SAVE = 1;
    public static final int MAX_FACES_TO_RECOGNIZE = 2;

    private final FacesApiClient facesApiClient;
    private final FaceDao faceDao;
    private final FaceCacheProvider faceCacheProvider;
    private final ImageExtensionValidator imageValidator;
    private final FaceClassifierPredictor classifierPredictor;
    private final FacesApiClient client;
    private final FacesMapper faceMapper;

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
            final MultipartFile file,
            final String faceName,
            final Double detProbThreshold,
            final String modelKey
    ) throws IOException {
        imageValidator.validate(file);
        FindFacesResponse findFacesResponse = facesApiClient.findFacesWithCalculator(file, MAX_FACES_TO_RECOGNIZE, detProbThreshold, null);
        val result = findFacesResponse.getResult();

        if (result.size() > MAX_FACES_TO_SAVE) {
            throw new TooManyFacesException();
        }

        val embedding = result.stream()
                .findFirst().orElseThrow()
                .getEmbedding();

        val embeddingToSave = new Face.Embedding(Arrays.asList(embedding), findFacesResponse.getPluginsVersions().getCalculator());

        FaceBO faceBO = faceCacheProvider
                .getOrLoad(modelKey)
                .addFace(faceDao.addNewFace(embeddingToSave, file, faceName, modelKey));
        return faceMapper.toResponseDto(faceBO);
    }

    public Map<String, List<FaceVerification>> verifyFace(ProcessImageParams processImageParams) {
        MultipartFile file = (MultipartFile) processImageParams.getFile();
        imageValidator.validate(file);

        FindFacesResponse findFacesResponse = client.findFacesWithCalculator(file, processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());

        val results = new ArrayList<FaceVerification>();

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

            PluginsVersions pluginsVersions = findFacesResponse.getPluginsVersions();
        }

        return Map.of("result", results);
    }
}