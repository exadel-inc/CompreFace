package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.enums.RetrainOption.NO;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.core.trainservice.TrainServiceApplication;
import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import com.exadel.frs.core.trainservice.config.WebMvcTestContext;
import com.exadel.frs.core.trainservice.filter.SecurityValidationFilter;
import com.exadel.frs.core.trainservice.service.RetrainService;
import com.exadel.frs.core.trainservice.service.ScanService;
import com.exadel.frs.core.trainservice.system.SystemService;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityValidationFilter.class}
        ))
@WebMvcTestContext
class ScanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScanService scanService;

    @MockBean
    private ImageExtensionValidator imageValidator;

    private final static String API_KEY = "api_key:model_key";

    @Test
    void scanFaces() throws Exception {
        val mockFile = new MockMultipartFile("file", "test data".getBytes());

        mockMvc.perform(
                multipart(API_V1 + "/faces/name")
                        .file(mockFile)
                        .param("retrain", NO.name())
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isCreated());

        verify(imageValidator).validate(any());
        verify(scanService).scanAndSaveFace(any(), any(), any(), any());
        verifyNoMoreInteractions(imageValidator, scanService);
    }
}