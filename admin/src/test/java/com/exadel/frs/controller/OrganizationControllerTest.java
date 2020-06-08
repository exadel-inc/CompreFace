package com.exadel.frs.controller;

import static com.exadel.frs.utils.TestUtils.buildUser;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.dto.ui.OrgResponseDto;
import com.exadel.frs.mapper.OrganizationMapper;
import com.exadel.frs.mapper.UserOrgRoleMapper;
import com.exadel.frs.service.OrganizationService;
import com.exadel.frs.system.security.JwtAuthenticationFilter;
import com.exadel.frs.system.security.config.AuthServerConfig;
import com.exadel.frs.system.security.config.ResourceServerConfig;
import com.exadel.frs.system.security.config.WebSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrganizationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, WebSecurityConfig.class, AuthServerConfig.class, ResourceServerConfig.class}
        )
)
public class OrganizationControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    UserOrgRoleMapper orgRoleMapper;

    @MockBean
    OrganizationMapper orgMapper;

    @MockBean
    private OrganizationService organizationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnOrganizations() throws Exception {
        val getOrgsRequest = get("/orgs").with(user(buildUser()));
        val dto = new OrgResponseDto("id", "name", "role");
        val expected = String.format(
                "[{\"id\":\"%s\",\"name\":\"%s\",\"role\":\"%s\"}]",
                dto.getId(),
                dto.getName(),
                dto.getRole()
        );

        when(orgMapper.toResponseDto(anyList(), anyLong())).thenReturn(List.of(dto));

        mockMvc.perform(getOrgsRequest)
               .andExpect(status().isOk())
               .andExpect(content().string(expected));

        verify(organizationService).getOrganizations(anyLong());
        verify(orgMapper).toResponseDto(anyList(), anyLong());
        verifyNoMoreInteractions(organizationService, orgMapper);
        verifyNoInteractions(orgRoleMapper);
    }
}
