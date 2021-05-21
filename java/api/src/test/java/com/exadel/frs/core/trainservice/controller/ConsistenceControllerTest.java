package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesStatusResponse;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@IntegrationTest
@AutoConfigureMockMvc
class ConsistenceControllerTest extends EmbeddedPostgreSQLTest {

    @MockBean
    FacesApiClient facesApiClient;

    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldReturnStatusWithoutAuth() throws Exception {
        when(facesApiClient.getStatus())
                .thenReturn(new FacesStatusResponse().setCalculatorVersion("Facenet2018"));

        mockMvc.perform(get(API_V1 + "/consistence/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saveImagesToDB", is(true)));
    }
}