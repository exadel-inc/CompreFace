package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FacesBox;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@AutoConfigureMockMvc
public class VerifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FaceClassifierPredictor predictor;

    @MockBean
    private ImageExtensionValidator validator;

    @MockBean
    private FacesApiClient client;

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

        when(client.findFacesWithCalculator(any(), any(), any(), isNull())).thenReturn(findFacesResponse);

        val firstFile = new MockMultipartFile("processFile", "test data".getBytes());
        val secondFile = new MockMultipartFile("checkFile", "test data".getBytes());

        mockMvc.perform(
                multipart(API_V1 + "/verify")
                        .file(firstFile)
                        .file(secondFile)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk());

        verify(validator, times(2)).validate(any());
        verify(client, times(2)).findFacesWithCalculator(any(), any(), any(), isNull());
        verify(predictor).verify(any(), any(double[][].class));
        verifyNoMoreInteractions(validator, client, predictor);
    }
}
