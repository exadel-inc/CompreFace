package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.repository.FacesRepositoryTest.makeFace;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.core.trainservice.config.WebMvcTestContext;
import com.exadel.frs.core.trainservice.dao.ModelDao;
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.entity.Model;
import com.exadel.frs.core.trainservice.filter.SecurityValidationFilter;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import com.exadel.frs.core.trainservice.system.SystemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityValidationFilter.class}
        ))
@WebMvcTestContext
public class FaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final static String API_KEY = "model_key";

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private FacesRepository facesRepository;

    @MockBean
    private ModelDao modelDao;

    @Autowired
    private SystemService systemService;

    @Test
    public void findAllShouldReturnResponseAsExpected() throws Exception {
        val token = systemService.buildToken(API_KEY);
        val faces = List.of(
                makeFace("A", token.getModelApiKey()),
                makeFace("B", token.getModelApiKey())
        );

        doReturn(faces)
                .when(facesRepository)
                .findByApiKey(token.getModelApiKey());

        val expectedContent = mapper.writeValueAsString(Map.of("names", new String[]{"A", "B"}));

        mockMvc.perform(get(API_V1 + "/faces").header(X_FRS_API_KEY_HEADER, API_KEY))
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
                .deleteFacesByApiKey(API_KEY);

        mockMvc.perform(delete(API_V1 + "/faces").header(X_FRS_API_KEY_HEADER, API_KEY))
               .andExpect(status().isOk())
               .andExpect(content().string(String.valueOf(response.size())));
    }

    @Test
    public void deleteFacesShouldReturnBadRequestWhenAppGuidIsMissing() throws Exception {
        mockMvc.perform(delete(API_V1 + "/faces"))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void updateModelKeySuccess() throws Exception {
        val newModelKey = UUID.randomUUID().toString();

        doReturn(Model.builder().build())
                .when(modelDao).updateModelApiKey(any(), any());

        mockMvc.perform(put(API_V1 + "/faces/api-key?new_model_api_key=" + newModelKey).header(X_FRS_API_KEY_HEADER, API_KEY))
               .andExpect(status().isOk());
    }
}