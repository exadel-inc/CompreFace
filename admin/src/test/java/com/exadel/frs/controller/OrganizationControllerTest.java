/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.controller;

import static com.exadel.frs.enums.OrganizationRole.OWNER;
import static com.exadel.frs.enums.OrganizationRole.USER;
import static com.exadel.frs.utils.TestUtils.buildUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.dto.ui.OrgResponseDto;
import com.exadel.frs.dto.ui.UserRoleResponseDto;
import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.UserOrganizationRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.mapper.OrganizationMapper;
import com.exadel.frs.mapper.UserOrgRoleMapper;
import com.exadel.frs.service.OrganizationService;
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
                classes = { WebSecurityConfig.class, AuthServerConfig.class, ResourceServerConfig.class}
        )
)
public class OrganizationControllerTest {

    private static final String ORG_GUID = "org-guid";

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

    @Test
    void shouldReturnOrganization() throws Exception {
        val getRequest = get("/org/" + ORG_GUID)
                .with(user(buildUser()));
        val responseDto = new OrgResponseDto("id", "name", "role");

        when(organizationService.getOrganization(eq(ORG_GUID), anyLong())).thenReturn(new Organization());
        when(orgMapper.toResponseDto(any(Organization.class), anyLong())).thenReturn(responseDto);

        mockMvc.perform(getRequest)
               .andExpect(status().isOk())
               .andExpect(content().string(mapper.writeValueAsString(responseDto)));

        verify(organizationService).getOrganization(eq(ORG_GUID), anyLong());
        verify(orgMapper).toResponseDto(any(Organization.class), anyLong());
        verifyNoMoreInteractions(organizationService, orgMapper);
        verifyNoInteractions(orgRoleMapper);
    }

    @Test
    void shouldReturnOrganizationRoles() throws Exception {
        val getRequest = get("/org/roles")
                .with(user(buildUser()));

        val expectedResult = OrganizationRole.values();

        mockMvc.perform(getRequest)
               .andExpect(status().isOk())
               .andExpect(content().string(mapper.writeValueAsString(expectedResult)));

        verifyNoInteractions(organizationService, orgMapper, orgRoleMapper);
    }

    @Test
    void shouldReturnOrganizationRolesToAssign() throws Exception {
        val getRequest = get("/org/" + ORG_GUID + "/assign-roles")
                .with(user(buildUser()));
        val responseArray = new OrganizationRole[]{OWNER, USER};

        when(organizationService.getOrgRolesToAssign(eq(ORG_GUID), anyLong())).thenReturn(responseArray);

        mockMvc.perform(getRequest)
               .andExpect(status().isOk())
               .andExpect(content().string(mapper.writeValueAsString(responseArray)));

        verify(organizationService).getOrgRolesToAssign(eq(ORG_GUID), anyLong());
        verifyNoMoreInteractions(organizationService);
        verifyNoInteractions(orgRoleMapper, orgMapper);
    }

    @Test
    void shouldReturnOrganizationUsersRoles() throws Exception {
        val getRequest = get("/org/" + ORG_GUID + "/roles")
                .with(user(buildUser()));

        val responseDto = new UserRoleResponseDto();
        responseDto.setFirstName("Test name");
        val responseDtoList = List.of(responseDto, responseDto);

        when(orgRoleMapper.toUserRoleResponseDto(anyList())).thenReturn(responseDtoList);

        mockMvc.perform(getRequest)
               .andExpect(status().isOk())
               .andExpect(content().string(mapper.writeValueAsString(responseDtoList)));

        verify(organizationService).getOrgUsers(eq(ORG_GUID), anyLong());
        verify(orgRoleMapper).toUserRoleResponseDto(anyList());
        verifyNoMoreInteractions(organizationService, orgRoleMapper);
        verifyNoInteractions(orgMapper);
    }

    @Test
    void shouldReturnUpdatedOrganizationUserRole() throws Exception {
        val updateDto = new UserRoleUpdateDto();
        updateDto.setRole(USER.name());
        updateDto.setUserId("user-id");

        val getRequest = put("/org/" + ORG_GUID + "/role")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateDto));

        val responseDto = new UserRoleResponseDto();
        responseDto.setUserId(updateDto.getUserId());

        when(organizationService.updateUserOrgRole(any(UserRoleUpdateDto.class), eq(ORG_GUID),anyLong())).thenReturn(new UserOrganizationRole());
        when(orgRoleMapper.toUserRoleResponseDto(any(UserOrganizationRole.class))).thenReturn(responseDto);

        mockMvc.perform(getRequest)
               .andExpect(status().isOk())
               .andExpect(content().string(mapper.writeValueAsString(responseDto)));

        verify(organizationService).updateUserOrgRole(any(UserRoleUpdateDto.class), eq(ORG_GUID), anyLong());
        verify(orgRoleMapper).toUserRoleResponseDto(any(UserOrganizationRole.class));
        verifyNoMoreInteractions(organizationService, orgRoleMapper);
        verifyNoInteractions(orgMapper);
    }
}