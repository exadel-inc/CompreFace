package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FacesBox;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import java.util.Arrays;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
public class VerifyControllerTest extends EmbeddedPostgreSQLTest {

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
        double[] embedding = {1.0};
        val findFacesResponse = FindFacesResponse.builder()
                .result(List.of(FindFacesResult.builder()
                        .embedding(Arrays.stream(embedding).boxed().toArray(Double[]::new))
                        .box(new FacesBox().setProbability(1D))
                        .build()
                ))
                .build();

        when(client.findFacesWithCalculator(any(), any(), any(), isNull())).thenReturn(findFacesResponse);
        when(predictor.verify(any(), any())).thenReturn(embedding);

        val firstFile = new MockMultipartFile("source_image", "test data".getBytes());
        val secondFile = new MockMultipartFile("target_image", "test data".getBytes());

        mockMvc.perform(
                multipart(API_V1 + "/verification/verify")
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
