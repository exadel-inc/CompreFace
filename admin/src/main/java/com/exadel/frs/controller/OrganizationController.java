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

import static com.exadel.frs.system.global.Constants.GUID_EXAMPLE;
import com.exadel.frs.dto.ui.OrgResponseDto;
import com.exadel.frs.dto.ui.UserRoleResponseDto;
import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.mapper.OrganizationMapper;
import com.exadel.frs.mapper.UserOrgRoleMapper;
import com.exadel.frs.service.OrganizationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final OrganizationMapper organizationMapper;
    private final UserOrgRoleMapper userOrgRoleMapper;

    @GetMapping("/org/{guid}")
    @ApiOperation(value = "Get organization")
    public OrgResponseDto getOrganization(
            @ApiParam(value = "GUID of organization to return", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String guid
    ) {
        return organizationMapper.toResponseDto(
                organizationService.getOrganization(guid, SecurityUtils.getPrincipalId()),
                SecurityUtils.getPrincipalId()
        );
    }

    @GetMapping("/orgs")
    @ApiOperation(value = "Get all organizations, the user is a member of")
    public List<OrgResponseDto> getOrganizations() {
        return organizationMapper.toResponseDto(
                organizationService.getOrganizations(SecurityUtils.getPrincipalId()),
                SecurityUtils.getPrincipalId()
        );
    }

    @GetMapping("/org/roles")
    @ApiOperation(value = "Get all user roles for organization")
    public OrganizationRole[] getOrgRoles() {
        return OrganizationRole.values();
    }

    @GetMapping("/org/{guid}/assign-roles")
    @ApiOperation(value = "Get organization roles, that can be assigned to other users")
    public OrganizationRole[] getOrgRolesToAssign(
            @ApiParam(value = "GUID of the organization", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String guid
    ) {
        return organizationService.getOrgRolesToAssign(guid, SecurityUtils.getPrincipalId());
    }

    @GetMapping("/org/{guid}/roles")
    @ApiOperation(value = "Get all users of organization")
    public List<UserRoleResponseDto> getOrgUsers(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String guid
    ) {
        return userOrgRoleMapper.toUserRoleResponseDto(
                organizationService.getOrgUsers(guid, SecurityUtils.getPrincipalId())
        );
    }

    @PutMapping("/org/{guid}/role")
    @ApiOperation(value = "Update user organization role")
    public UserRoleResponseDto updateUserOrgRole(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String guid,
            @ApiParam(value = "User role data", required = true)
            @Valid
            @RequestBody
            final UserRoleUpdateDto userRoleUpdateDto
    ) {
        final Long admin = SecurityUtils.getPrincipalId();
        val updatedUserOrgRole = organizationService.updateUserOrgRole(userRoleUpdateDto, guid, admin);

        return userOrgRoleMapper.toUserRoleResponseDto(updatedUserOrgRole);
    }
}