package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesBox;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.commonservice.system.global.Constants;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.dto.VerifySourceTargetRequest;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.List;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@AutoConfigureMockMvc
class VerifyControllerTest extends EmbeddedPostgreSQLTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FaceClassifierPredictor predictor;

    @MockBean
    private ImageExtensionValidator validator;

    @MockBean
    private FacesApiClient client;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String MODEL_KEY = "model_key";
    private static final String API_KEY = MODEL_KEY;

    @Test
    void verifyFaces() throws Exception {
        val findFacesResponse = FindFacesResponse.builder()
                .result(List.of(FindFacesResult.builder()
                        .embedding(new Double[]{1.0})
                        .box(new FacesBox().setProbability(1D))
                        .build()
                ))
                .build();

        when(client.findFacesWithCalculator(any(), any(), any(), isNull(), any())).thenReturn(findFacesResponse);
        when(predictor.verify(any(), any())).thenReturn(new double[]{100d});

        val firstFile = new MockMultipartFile("source_image", "test data".getBytes());
        val secondFile = new MockMultipartFile("target_image", "test data".getBytes());

        mockMvc.perform(
                multipart(API_V1 + "/verification/verify")
                        .file(firstFile)
                        .file(secondFile)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk());

        verify(validator, times(2)).validate(any());
        verify(client, times(2)).findFacesWithCalculator(any(), any(), any(), isNull(), any());
        verify(predictor).verify(any(), any(double[][].class));
        verifyNoMoreInteractions(validator, client, predictor);
    }

    @Test
    void verifyFacesBase64() throws Exception {
        val findFacesResponse = FindFacesResponse.builder()
                .result(List.of(FindFacesResult.builder()
                        .embedding(new Double[]{1.0})
                        .box(new FacesBox().setProbability(1D))
                        .build()
                ))
                .build();

        when(client.findFacesBase64WithCalculator(any(), any(), any(), anyString(), any())).thenReturn(findFacesResponse);
        when(predictor.verify(any(), any())).thenReturn(new double[]{100d});

        VerifySourceTargetRequest request = new VerifySourceTargetRequest();
        request.setSourceImageBase64(Base64.getEncoder().encodeToString(new byte[]{(byte) 0xCA}));
        request.setTargetImageBase64(Base64.getEncoder().encodeToString(new byte[]{(byte) 0xCA}));

        mockMvc.perform(
                post(API_V1 + "/verification/verify")
                        .queryParam("limit", "4")
                        .queryParam(Constants.DET_PROB_THRESHOLD, "0.7")
                        .queryParam(Constants.FACE_PLUGINS, "faceplug")
                        .contentType(MediaType.APPLICATION_JSON_VALUE).content(objectMapper.writeValueAsString(request))
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk());

        verify(validator, times(2)).validateBase64(any());
        verify(client, times(2)).findFacesBase64WithCalculator(any(), any(), any(), anyString(), any());
        verify(predictor).verify(any(), any(double[][].class));

        verifyNoMoreInteractions(validator, client, predictor);
    }
}
