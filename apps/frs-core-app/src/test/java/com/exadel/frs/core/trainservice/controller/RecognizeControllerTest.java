package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.core.trainservice.repository.FaceClassifierAdapter;
import com.exadel.frs.core.trainservice.repository.FaceClassifierStorage;
import com.exadel.frs.core.trainservice.system.SystemService;
import com.exadel.frs.core.trainservice.system.Token;
import com.exadel.frs.core.trainservice.system.python.ScanBox;
import com.exadel.frs.core.trainservice.system.python.ScanFacesClient;
import com.exadel.frs.core.trainservice.system.python.ScanResponse;
import com.exadel.frs.core.trainservice.system.python.ScanResult;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RecognizeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FaceClassifierStorage storage;

    @MockBean
    private ScanFacesClient client;

    @MockBean
    private SystemService systemService;

    @MockBean
    private ImageExtensionValidator imageValidator;

    private final static String APP_KEY = "api_key";
    private final static String MODEL_KEY = ":model_key";
    private final static String API_KEY = APP_KEY + MODEL_KEY;

    @Test
    void recognize() throws Exception {
        val mockAdapter = mock(FaceClassifierAdapter.class);
        val mockFile = new MockMultipartFile("file", "test data".getBytes());
        val scanResponse = new ScanResponse().setResult(
                List.of(new ScanResult()
                        .setEmbedding(List.of(1.0))
                        .setBox(new ScanBox().setProbability(1D))
                )
        );

        when(systemService.buildToken(API_KEY)).thenReturn(new Token(APP_KEY, MODEL_KEY));
        when(storage.isLocked(APP_KEY, MODEL_KEY)).thenReturn(false);
        when(client.scanFaces(any(), any(), any())).thenReturn(scanResponse);
        when(storage.getFaceClassifier(APP_KEY, MODEL_KEY)).thenReturn(mockAdapter);
        when(mockAdapter.predict(any())).thenReturn(Pair.of(1, ""));

        mockMvc.perform(
                multipart(API_V1 + "/recognize")
                        .file(mockFile)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk());
    }
}