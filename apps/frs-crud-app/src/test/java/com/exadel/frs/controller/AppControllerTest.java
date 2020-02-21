package com.exadel.frs.controller;

import com.exadel.frs.dto.ui.AppUpdateDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.exception.AppNotFoundException;
import com.exadel.frs.exception.BasicException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.FieldRequiredException;
import com.exadel.frs.mapper.AppMapper;
import com.exadel.frs.mapper.UserAppRoleMapper;
import com.exadel.frs.service.AppService;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static com.exadel.frs.utils.TestUtils.USER_ID;
import static com.exadel.frs.utils.TestUtils.buildUser;
import static com.exadel.frs.utils.TestUtils.buildExceptionResponse;
import static com.exadel.frs.utils.TestUtils.buildUndefinedExceptionResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AppController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, WebSecurityConfig.class, AuthServerConfig.class, ResourceServerConfig.class})
)
@MockBeans({@MockBean(AppMapper.class), @MockBean(UserAppRoleMapper.class)})
class AppControllerTest {

    private static final long APP_ID = 1L;
    private static final String APP_GUID = "app-guid";
    private static final String ORG_GUID = "org-guid";

    @MockBean
    private AppService appService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldReturnMessageAndCodeWhenAppNotFoundExceptionThrown() throws Exception {
        final BasicException expectedException = new AppNotFoundException(APP_GUID);

        when(appService.getApp(APP_GUID, USER_ID)).thenThrow(expectedException);

        String expectedContent = mapper.writeValueAsString(buildExceptionResponse(expectedException));
        mockMvc.perform(get("/org/" + ORG_GUID + "/app/" + APP_GUID).with(user(buildUser())))
                .andExpect(status().isNotFound())
                .andExpect(content().string(expectedContent));
    }

    @Test
    public void shouldReturnMessageAndCodeWhenUnexpectedExceptionThrown() throws Exception {
        final Exception expectedException = new NullPointerException("Something went wrong, please try again");

        when(appService.getApps(ORG_GUID, USER_ID)).thenThrow(expectedException);

        String expectedContent = mapper.writeValueAsString(buildUndefinedExceptionResponse(expectedException));
        mockMvc.perform(get("/org/" + ORG_GUID + "/apps").with(user(buildUser())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedContent));
    }

    @Test
    public void shouldReturnMessageAndCodeWhenAppNameIsMissing() throws Exception {
        final BasicException expectedException = new EmptyRequiredFieldException("name");

        doThrow(expectedException).when(appService).createApp(any(), eq(ORG_GUID), eq(USER_ID));

        MockHttpServletRequestBuilder request = post("/org/" + ORG_GUID + "/app")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(App.builder().id(APP_ID).build()));

        val expectedContent = mapper.writeValueAsString(buildExceptionResponse(expectedException));
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedContent));
    }

    @Test
    public void shouldReturn400AndErrorMessageWhenRenameAppToEmpty() throws Exception {
        doCallRealMethod().when(appService).updateApp(any(), any(), any());
        String expectedContent = mapper.writeValueAsString(buildExceptionResponse(new FieldRequiredException("Application name")));

        val bodyWithEmptyName = new AppUpdateDto();
        bodyWithEmptyName.setName("");
        val bodyWithNoName = new AppUpdateDto();

        val updateRequest = put("/org/" + ORG_GUID + "/app/" + APP_GUID)
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