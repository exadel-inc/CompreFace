package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.core.trainservice.config.WebMvcTestContext;
import com.exadel.frs.core.trainservice.dao.ModelDao;
import com.exadel.frs.core.trainservice.entity.mongo.Model;
import com.exadel.frs.core.trainservice.filter.SecurityValidationFilter;
import com.exadel.frs.core.trainservice.repository.mongo.FacesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class ModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final static String API_KEY = "model_key";

    @MockBean
    private ModelDao modelDao;

    @Test
    public void updateModelKeySuccess() throws Exception {
        val newModelKey = UUID.randomUUID().toString();

        doReturn(Model.builder().build())
                .when(modelDao).updateModelApiKey(any(), any());

        mockMvc.perform(put(API_V1 + "/models/api-key?new_model_api_key=" + newModelKey).header(X_FRS_API_KEY_HEADER, API_KEY))
               .andExpect(status().isOk());
    }
}
