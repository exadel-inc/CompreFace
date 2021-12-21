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

package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.EmbeddingProjection;
import com.exadel.frs.commonservice.entity.Img;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.exception.EmbeddingNotFoundException;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.PluginsVersions;
import com.exadel.frs.commonservice.system.global.Constants;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.ItemsBuilder;
import com.exadel.frs.core.trainservice.cache.EmbeddingCacheProvider;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.dto.Base64File;
import com.exadel.frs.core.trainservice.dto.FaceVerification;
import com.exadel.frs.core.trainservice.service.EmbeddingService;
import com.exadel.frs.core.trainservice.service.SubjectService;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@AutoConfigureMockMvc
class EmbeddingControllerTest extends EmbeddedPostgreSQLTest {

    private static final String INCORRECT_PART_NAME = "File";
    private static final String API_KEY = "api_key";

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EmbeddingCacheProvider embeddingCacheProvider;

    @MockBean
    SubjectService subjectService;

    @MockBean
    EmbeddingService embeddingService;

    @MockBean
    ImageExtensionValidator imageValidator;

    @MockBean
    FaceClassifierPredictor predictor;

    @MockBean
    FacesApiClient client;

    private static Pair<Subject, Embedding> pair() {
        Subject subject = new Subject();
        subject.setId(UUID.randomUUID());

        Embedding embedding = new Embedding();
        embedding.setId(UUID.randomUUID());

        return Pair.of(subject, embedding);
    }

    @Test
    void testAddEmbedding() throws Exception {
        var mockFile = new MockMultipartFile("file", "test data".getBytes());

        doReturn(pair())
                .when(subjectService)
                .saveCalculatedEmbedding(any(MultipartFile.class), any(), any(), any());

        mockMvc.perform(
                multipart(API_V1 + "/recognition/faces")
                        .file(mockFile)
                        .param("subject", "name")
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isCreated());

        verify(imageValidator).validate(any());
    }

    @Test
    void testAddEmbeddingBase64() throws Exception {
        doReturn(pair())
                .when(subjectService)
                .saveCalculatedEmbedding(any(String.class), any(), any(), any());

        Base64File request = new Base64File();
        request.setContent(Base64.getEncoder().encodeToString(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE}));

        mockMvc.perform(
                post(API_V1 + "/recognition/faces")
                        .queryParam("subject", "subject")
                        .queryParam(Constants.DET_PROB_THRESHOLD, "0.7")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isCreated());

        verify(imageValidator).validateBase64(any());
    }

    @Test
    void testDownloadImg() throws Exception {
        var embeddingId = UUID.randomUUID();
        final byte[] content = {0xC, 0xA};

        Img img = new Img();
        img.setContent(content);

        when(embeddingService.getImg(API_KEY, embeddingId))
                .thenReturn(Optional.of(img));

        mockMvc.perform(
                get(API_V1 + "/recognition/faces/{embeddingId}/img", embeddingId)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk())
                .andExpect(content().bytes(content));
    }

    @Test
    void testDownloadImgNotFound() throws Exception {
        var embeddingId = UUID.randomUUID();
        when(embeddingService.getImg(API_KEY, embeddingId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                get(API_V1 + "/recognition/faces/{embeddingId}/img", embeddingId)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{}));
    }

    @Test
    void testListEmbeddings() throws Exception {
        when(embeddingService.listEmbeddings(eq(API_KEY), eq(null), any()))
                .thenReturn(new PageImpl<>(
                        List.of(
                                new EmbeddingProjection(UUID.randomUUID(), "name1"),
                                new EmbeddingProjection(UUID.randomUUID(), "name2")
                        ),
                        PageRequest.of(1, 10), // second page
                        12
                ));

        mockMvc.perform(
                get(API_V1 + "/recognition/faces")
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.faces.length()", is(2)))
                .andExpect(jsonPath("$.page_number", is(1))) // page number
                .andExpect(jsonPath("$.page_size", is(10))) // page size
                .andExpect(jsonPath("$.total_pages", is(2)))
                .andExpect(jsonPath("$.total_elements", is(12)));
    }

    @Test
    void testListEmbeddingsWithSubjectName() throws Exception {
        var subjectName = "Johnny Depp";
        when(embeddingService.listEmbeddings(eq(API_KEY), eq(subjectName), any()))
                .thenReturn(new PageImpl<>(
                        List.of(new EmbeddingProjection(UUID.randomUUID(), subjectName)),
                        PageRequest.of(1, 10), // second page
                        12
                ));

        mockMvc.perform(
                get(API_V1 + "/recognition/faces")
                        .queryParam("subject", subjectName)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.faces.length()", is(1)))
                .andExpect(jsonPath("$.page_number", is(1))) // page number
                .andExpect(jsonPath("$.page_size", is(10))) // page size
                .andExpect(jsonPath("$.total_pages", is(2)))
                .andExpect(jsonPath("$.total_elements", is(11)));
    }

    @Test
    void testListEmbeddingsFail() throws Exception {
        var expectedContent = "{\"message\":\"" + String.format("Missing header: %s", X_FRS_API_KEY_HEADER) + "\",\"code\":20}";

        mockMvc.perform(get(API_V1 + "/recognition/faces"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedContent));
    }

    @Test
    void testRemoveSubjectEmbedding() throws Exception {
        var subjectName = "subject_name";
        var deleted = 5;

        when(subjectService.removeAllSubjectEmbeddings(API_KEY, subjectName))
                .thenReturn(deleted);

        mockMvc.perform(
                delete(API_V1 + "/recognition/faces")
                        .queryParam("subject", subjectName)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted", is(deleted)));
    }

    @Test
    void testDeleteEmbeddingById() throws Exception {
        Embedding embedding = ItemsBuilder.makeEmbedding("subject_name", API_KEY);
        embedding.setId(UUID.randomUUID());

        when(subjectService.removeSubjectEmbedding(API_KEY, embedding.getId()))
                .thenReturn(embedding);

        mockMvc.perform(
                delete(API_V1 + "/recognition/faces/{embeddingId}", embedding.getId())
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.image_id", is(embedding.getId().toString())));
    }

    @Test
    void testDeleteEmbeddingByIdNotFound() throws Exception {
        var embeddingId = UUID.randomUUID();

        when(subjectService.removeSubjectEmbedding(API_KEY, embeddingId))
                .thenThrow(new EmbeddingNotFoundException(embeddingId));

        mockMvc.perform(
                delete(API_V1 + "/recognition/faces/{embeddingId}", embeddingId)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isNotFound());
    }

    @Test
    void verifyFaces() throws Exception {
        var embeddingId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", new byte[]{0xA});

        int count = 4;
        when(subjectService.verifyFace(any()))
                .thenReturn(verificationResult(count));

        mockMvc.perform(
                multipart(API_V1 + "/recognition/faces/{embeddingId}/verify", embeddingId)
                        .file(file)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()", is(count)));

        verify(imageValidator).validate(any());
    }

    private static Pair<List<FaceVerification>, PluginsVersions> verificationResult(int count) {
        return Pair.of(
                IntStream.range(0, count)
                        .mapToObj(i -> FaceVerification.builder().subject("" + i).build())
                        .collect(Collectors.toList()),
                PluginsVersions.builder().calculator("calc").build()
        );
    }

    @Test
    void verifyFacesBase64() throws Exception {
        var embeddingId = UUID.randomUUID();

        int count = 5;
        when(subjectService.verifyFace(any()))
                .thenReturn(verificationResult(count));

        final Base64File img = new Base64File();
        img.setContent(Base64.getEncoder().encodeToString(new byte[]{0xA}));

        mockMvc.perform(
                post(API_V1 + "/recognition/faces/{embeddingId}/verify", embeddingId)
                        .content(objectMapper.writeValueAsString(img)).contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()", is(count)));

        verify(imageValidator).validateBase64(any());
    }

    @Test
    void verifyFacesWithIncorrectPartName() throws Exception {
        var mockFile = new MockMultipartFile("file", "test data".getBytes());

        MvcResult mvcResult = mockMvc.perform(
                multipart(API_V1 + "/recognition/faces/{embeddingId}/verify", UUID.randomUUID())
                        .file(INCORRECT_PART_NAME, mockFile.getBytes())
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isBadRequest()).andReturn();

        Assertions.assertTrue(mvcResult.getResponse().getContentAsString().contains("Required part "));
    }
}