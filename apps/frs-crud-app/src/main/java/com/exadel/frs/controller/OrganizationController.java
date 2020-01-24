package com.exadel.frs.controller;

import com.exadel.frs.dto.ui.*;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.mapper.OrganizationMapper;
import com.exadel.frs.mapper.UserOrgRoleMapper;
import com.exadel.frs.service.OrganizationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class OrganizationController {

    private static final String GUID_EXAMPLE = "3913717b-a40b-4d6f-acc4-a861aa612651";

    private final OrganizationService organizationService;
    private final OrganizationMapper organizationMapper;
    private final UserOrgRoleMapper userOrgRoleMapper;

    @GetMapping("/org/{guid}")
    @ApiOperation(value = "Get organization")
    public OrgResponseDto getOrganization(
            @ApiParam(value = "GUID of organization to return", required = true, example = GUID_EXAMPLE) @PathVariable final String guid) {
        return organizationMapper.toResponseDto(organizationService.getOrganization(guid, SecurityUtils.getPrincipalId()), SecurityUtils.getPrincipalId());
    }

    @GetMapping("/orgs")
    @ApiOperation(value = "Get all organizations, the user is a member of")
    public List<OrgResponseDto> getOrganizations() {
        return organizationMapper.toResponseDto(organizationService.getOrganizations(SecurityUtils.getPrincipalId()), SecurityUtils.getPrincipalId());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/org")
    @ApiOperation(value = "Create organization")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Organization name is required")
    })
    public OrgResponseDto createOrganization(
            @ApiParam(value = "Organization object that needs to be created", required = true) @Valid @RequestBody OrgCreateDto orgCreateDto) {
        return organizationMapper.toResponseDto(organizationService.createOrganization(orgCreateDto, SecurityUtils.getPrincipalId()), SecurityUtils.getPrincipalId());
    }

    @PutMapping("/org/{guid}")
    @ApiOperation(value = "Update organization name")
    public void updateOrganization(
            @ApiParam(value = "GUID of organization that needs to be updated", required = true, example = GUID_EXAMPLE) @PathVariable final String guid,
            @ApiParam(value = "Organization data", required = true) @Valid @RequestBody OrgUpdateDto orgUpdateDto) {
        organizationService.updateOrganization(orgUpdateDto, guid, SecurityUtils.getPrincipalId());
    }

    @DeleteMapping("/org/{guid}")
    @ApiOperation(value = "Delete organization")
    public void deleteOrganization(
            @ApiParam(value = "GUID of the organization that needs to be deleted", required = true, example = GUID_EXAMPLE) @PathVariable final String guid) {
        organizationService.deleteOrganization(guid, SecurityUtils.getPrincipalId());
    }

    @GetMapping("/org/roles")
    @ApiOperation(value = "Get all user roles for organization")
    public OrganizationRole[] getOrgRoles() {
        return OrganizationRole.values();
    }

    @GetMapping("/org/{guid}/roles")
    @ApiOperation(value = "Get all users of organization")
    public List<UserRoleResponseDto> getOrgUsers(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE) @PathVariable final String guid) {
        return userOrgRoleMapper.toUserRoleResponseDto(organizationService.getOrgUsers(guid, SecurityUtils.getPrincipalId()));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/org/{guid}/invite")
    @ApiOperation(value = "Add users to organization")
    public UserRoleResponseDto inviteUser(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE) @PathVariable final String guid,
            @ApiParam(value = "User invite data", required = true) @Valid @RequestBody UserInviteDto userInviteDto) {
        return userOrgRoleMapper.toUserRoleResponseDto(organizationService.inviteUser(userInviteDto, guid, SecurityUtils.getPrincipalId()));
    }

    @PutMapping("/org/{guid}/role")
    @ApiOperation(value = "Update user organization role")
    public void updateUserOrgRole(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE) @PathVariable final String guid,
            @ApiParam(value = "User role data", required = true) @Valid @RequestBody UserRoleUpdateDto userRoleUpdateDto) {
        organizationService.updateUserOrgRole(userRoleUpdateDto, guid, SecurityUtils.getPrincipalId());
    }

    @PutMapping("/org/{guid}/remove")
    @ApiOperation(value = "Remove users from organization")
    public void removeUsersFromOrganization(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE) @PathVariable final String guid,
            @ApiParam(value = "User to remove", required = true) @Valid @RequestBody UserRemoveDto userRemoveDto) {
        organizationService.removeUserFromOrganization(userRemoveDto, guid, SecurityUtils.getPrincipalId());
    }

}
