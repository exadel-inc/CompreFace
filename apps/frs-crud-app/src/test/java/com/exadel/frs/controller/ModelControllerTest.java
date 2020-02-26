package com.exadel.frs.controller;

import com.exadel.frs.dto.ui.ModelUpdateDto;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.mapper.MlModelMapper;
import com.exadel.frs.service.ModelService;
import com.exadel.frs.system.security.JwtAuthenticationFilter;
import com.exadel.frs.system.security.config.AuthServerConfig;
import com.exadel.frs.system.security.config.ResourceServerConfig;
import com.exadel.frs.system.security.config.WebSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.exadel.frs.utils.TestUtils.buildUser;
import static com.exadel.frs.utils.TestUtils.buildExceptionResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ModelController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, WebSecurityConfig.class, AuthServerConfig.class, ResourceServerConfig.class}
        )
)
@MockBeans({@MockBean(MlModelMapper.class)})
class ModelControllerTest {

    private static final String ORG_GUID = "org-guid";
    private static final String APP_GUID = "app-guid";
    private static final String MODEL_GUID = "model-guid";

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ModelService modelService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnMessageAndCodeWhenModelNameIsMissing() throws Exception {
        doCallRealMethod().when(modelService).updateModel(any(), any(), any());
        val expectedContent = mapper.writeValueAsString(buildExceptionResponse(new EmptyRequiredFieldException("name")));
        val bodyWithEmptyName = new ModelUpdateDto();
        bodyWithEmptyName.setName("");

        val bodyWithNoName = new ModelUpdateDto();

        val updateRequest = put("/org/" + ORG_GUID + "/app/" + APP_GUID + "/model/" + MODEL_GUID)
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(updateRequest.content(mapper.writeValueAsString(bodyWithEmptyName)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedContent));

        mockMvc.perform(updateRequest.content(mapper.writeValueAsString(bodyWithNoName)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedContent));
    }
}