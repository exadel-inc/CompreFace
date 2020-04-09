package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.repository.FacesRepositoryTest.makeFace;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.core.trainservice.domain.Face;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class FaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final static String APP_GUID = "app_guid_for_test";

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private FacesRepository facesRepository;

    @Test
    public void findAllShouldReturnResponseAsExpected() throws Exception {
        var faces = List.of(makeFace("A", APP_GUID), makeFace("B", APP_GUID));
        doReturn(faces)
                .when(facesRepository)
                .findByApiKey(APP_GUID);

        var expectedContent = mapper.writeValueAsString(Map.of("names", new String[]{"A", "B"}));

        mockMvc.perform(get(API_V1 + "/faces").header(X_FRS_API_KEY_HEADER, APP_GUID))
               .andExpect(status().isOk())
               .andExpect(content().json(expectedContent));
    }

    @Test
    public void findAllShouldReturnBadRequestWhenAppGuidIsMissing() throws Exception {
        mockMvc.perform(get(API_V1 + "/faces"))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteFacesShouldReturnResponseAsExpected() throws Exception {
        val response = List.of(new Face(), new Face(), new Face());
        doReturn(response)
                .when(facesRepository)
                .deleteFacesByApiKey(APP_GUID.substring(APP_GUID.length() / 2));

        mockMvc.perform(delete(API_V1 + "/faces").header(X_FRS_API_KEY_HEADER, APP_GUID))
               .andExpect(status().isOk())
               .andExpect(content().string(String.valueOf(response.size())));
    }

    @Test
    public void deleteFacesShouldReturnBadRequestWhenAppGuidIsMissing() throws Exception {
        mockMvc.perform(delete(API_V1 + "/faces"))
               .andExpect(status().isBadRequest());
    }
}