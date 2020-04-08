package com.exadel.frs.controller;

import com.exadel.frs.dto.ui.OrgCreateDto;
import com.exadel.frs.mapper.OrganizationMapper;
import com.exadel.frs.mapper.UserOrgRoleMapper;
import com.exadel.frs.service.OrganizationService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrganizationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, WebSecurityConfig.class, AuthServerConfig.class, ResourceServerConfig.class}
        )
)
@MockBeans({@MockBean(OrganizationMapper.class), @MockBean(UserOrgRoleMapper.class)})
public class OrganizationControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private OrganizationService organizationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnErrorMessageWhenNameIsMissingOnCreateNewModel() throws Exception {
        val expectedContent = "{\"message\":\"Organization name cannot be empty\",\"code\":5}";
        val bodyWithEmptyName = new OrgCreateDto();
        bodyWithEmptyName.setName("");

        val bodyWithNoName = new OrgCreateDto();

        val createNewModelRequest = post("/org")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(createNewModelRequest.content(mapper.writeValueAsString(bodyWithEmptyName)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedContent));

        mockMvc.perform(createNewModelRequest.content(mapper.writeValueAsString(bodyWithNoName)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedContent));
    }
}
