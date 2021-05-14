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

import com.exadel.frs.commonservice.entity.App;
import com.exadel.frs.commonservice.entity.Face;
import com.exadel.frs.commonservice.enums.ModelType;
import com.exadel.frs.commonservice.repository.FacesRepository;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.PluginsVersions;
import com.exadel.frs.core.trainservice.util.MultipartFileData;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.exadel.frs.core.trainservice.ItemsBuilder.makeApp;
import static com.exadel.frs.core.trainservice.ItemsBuilder.makeFace;
import static com.exadel.frs.core.trainservice.ItemsBuilder.makeModel;
import static com.exadel.frs.core.trainservice.service.FaceService.MAX_FACES_TO_RECOGNIZE;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FaceServiceTestIT extends EmbeddedPostgreSQLTest {

    @Autowired
    private FacesRepository facesRepository;

    @Autowired
    private FaceService faceService;

    @MockBean
    private FacesApiClient facesApiClient;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final MultipartFile MULTIPART_FILE_DATA = new MultipartFileData("hex-string-1".getBytes(), "test", "application/json");
    private static final String FACE_NAME = "faceName";
    private static final double THRESHOLD = 1.0;
    private static final double EMBEDDING = 100500;
    private static final FindFacesResponse FIND_RESULT = FindFacesResponse.builder()
                                                                          .pluginsVersions(PluginsVersions.builder()
                                                                                                          .calculator("1.0")
                                                                                                          .build())
                                                                          .result(List.of(FindFacesResult.builder()
                                                                                                         .embedding(new Double[]{EMBEDDING})
                                                                                                         .build())
                                                                          )
                                                                          .build();

    private static final String MODEL_KEY = randomUUID().toString();
    private static final String MODEL_KEY_OTHER = randomUUID().toString();

    @BeforeEach
    void setUp() {
        App app = makeApp(1L, MODEL_KEY);
        jdbcTemplate.update("INSERT INTO App(id, name, guid, api_key) VALUES (?, ?, ?, ?)",
                app.getId(),
                app.getName(),
                app.getGuid(),
                app.getApiKey());

        modelRepository.saveAll(List.of(
                makeModel(MODEL_KEY, ModelType.RECOGNITION, app),
                makeModel(MODEL_KEY_OTHER, ModelType.RECOGNITION, app)
        ));

        facesRepository.saveAll(List.of(
                makeFace("A", MODEL_KEY),
                makeFace("B", MODEL_KEY_OTHER),
                makeFace("C", MODEL_KEY)
        ));
    }

    @Test
    public void deleteFaceByGuid() {
        List<Face> faces = facesRepository.findByApiKey(MODEL_KEY);
        Face face = faces.get(new Random().nextInt(faces.size()));

        faceService.deleteFaceById(face.getId(), MODEL_KEY);

        val actual = facesRepository.findByApiKey(MODEL_KEY);

        assertThat(actual).hasSize(faces.size() - 1);
        assertThat(actual).doesNotContain(face);
    }

    @Test
    public void findFaces() {
        val faces = facesRepository.findAll().stream()
                                   .filter(face -> face.getApiKey().equals(MODEL_KEY))
                                   .collect(toList());

        val actual = faceService.findFaces(MODEL_KEY);

        assertThat(actual).hasSize(faces.size());
    }

    @Test
    public void deleteFaceByName() {
        val faces = facesRepository.findByApiKey(MODEL_KEY);
        val face = faces.get(new Random().nextInt(faces.size()));

        faceService.deleteFaceByName(face.getFaceName(), face.getApiKey());

        val actual = facesRepository.findByApiKey(MODEL_KEY);

        assertThat(actual).hasSize(faces.size() - 1);
        assertThat(actual).doesNotContain(face);
    }

    @Test
    public void deleteFacesByModel() {
        val faces = facesRepository.findAll();
        val oneKeyFaces = faces.stream()
                               .filter(face -> face.getApiKey().equals(MODEL_KEY))
                               .collect(toList());

        faceService.deleteFacesByModel(MODEL_KEY);

        val actual = facesRepository.findAll();

        assertThat(actual).hasSize(faces.size() - 2);
        assertThat(oneKeyFaces).allSatisfy(face -> assertThat(actual).doesNotContain(face));
    }

    @Test
    public void countFacesInModel() {
        val faces = facesRepository.findAll();
        val oneKeyFaces = faces.stream()
                               .filter(face -> face.getApiKey().equals(MODEL_KEY))
                               .collect(Collectors.toList());

        val actual = faceService.countFacesInModel(MODEL_KEY);

        assertThat(actual).isEqualTo(oneKeyFaces.size());
    }

    @Test
    public void findAndSaveFaceTest() throws IOException {
        when(facesApiClient.findFacesWithCalculator(MULTIPART_FILE_DATA, MAX_FACES_TO_RECOGNIZE, THRESHOLD, null)).thenReturn(FIND_RESULT);

        val actual = faceService.findAndSaveFace(MULTIPART_FILE_DATA, FACE_NAME, THRESHOLD, MODEL_KEY);

        assertThat(actual).isNotNull();
        assertThat(actual.getSubjectName()).isEqualTo(FACE_NAME);
    }

    @AfterEach
    public void cleanUp() {
        faceService.deleteFacesByModel(MODEL_KEY);
        faceService.deleteFacesByModel(MODEL_KEY_OTHER);
        jdbcTemplate.execute("DELETE FROM App");
    }
}