package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.core.trainservice.domain.Face;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.exadel.frs.core.trainservice.repository.FacesRepositoryTest.makeFace;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FaceControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private final String APP_GUID = "app_guid_for_test";
    private ObjectMapper mapper = new ObjectMapper();
    @MockBean
    private FacesRepository facesRepository;

    @Test
    public void shouldReturnResponseAsExpected() throws Exception {
        List<Face> faces = Arrays.asList(makeFace("A", APP_GUID), makeFace("B", APP_GUID));
        doReturn(faces)
                .when(facesRepository)
                .findByApiKey(APP_GUID);


        String expectedContent = mapper.writeValueAsString(Map.of("names", new String[]{"A", "B"}));

        mockMvc.perform(get("/faces").header("x-frs-api-key", APP_GUID))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedContent));
    }

    @Test
    public void shouldReturnBadRequestWhenAppGuidIsMIssing() throws Exception {
        mockMvc.perform(get("/faces"))
                .andExpect(status().isBadRequest());
    }
}