package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesStatusResponse;
import com.exadel.frs.core.trainservice.DbHelper;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@AutoConfigureMockMvc
class ConsistenceControllerTest extends EmbeddedPostgreSQLTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FacesApiClient client;

    @Test
    void shouldReturnStatusWithoutAuth() throws Exception {
        var currentCalculator = "currentCalculator";

        when(client.getStatus())
                .thenReturn(new FacesStatusResponse().setCalculatorVersion(currentCalculator));

        var model = dbHelper.insertModel();
        var subject = dbHelper.insertSubject(model, "subject");
        dbHelper.insertEmbeddingWithImg(subject, currentCalculator + "any");

        mockMvc.perform(get(API_V1 + "/consistence/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saveImagesToDB", is(true)))
                .andExpect(jsonPath("$.dbIsInconsistent", is(true)))
                .andExpect(jsonPath("$.demoFaceCollectionIsInconsistent", is(false)));
    }
}