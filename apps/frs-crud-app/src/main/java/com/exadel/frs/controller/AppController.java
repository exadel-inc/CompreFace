package com.exadel.frs.controller;

import com.exadel.frs.dto.ui.*;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.mapper.AppMapper;
import com.exadel.frs.mapper.UserAppRoleMapper;
import com.exadel.frs.service.AppService;
import com.exadel.frs.system.statistics.CallStatistics;
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
public class AppController {

    private static final String GUID_EXAMPLE = "3913717b-a40b-4d6f-acc4-a861aa612651";

    private final AppService appService;
    private final AppMapper appMapper;
    private final UserAppRoleMapper userAppRoleMapper;

    @CallStatistics
    @GetMapping("/org/{orgGuid}/app/{guid}")
    @ApiOperation(value = "Get application")
    public AppResponseDto getApp(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE) @PathVariable String orgGuid,
            @ApiParam(value = "GUID of application to return", required = true, example = GUID_EXAMPLE) @PathVariable String guid) {
        return appMapper.toResponseDto(appService.getApp(guid, SecurityUtils.getPrincipalId()), SecurityUtils.getPrincipalId());
    }

    @GetMapping("/org/{orgGuid}/apps")
    @ApiOperation(value = "Get all applications in organization")
    public List<AppResponseDto> getApps(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE) @PathVariable String orgGuid) {
        return appMapper.toResponseDto(appService.getApps(orgGuid, SecurityUtils.getPrincipalId()), SecurityUtils.getPrincipalId());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/org/{orgGuid}/app")
    @ApiOperation(value = "Create application")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Application name is required")
    })
    public AppResponseDto createApp(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE) @PathVariable String orgGuid,
            @ApiParam(value = "Application object that needs to be created", required = true) @Valid @RequestBody AppCreateDto appCreateDto) {
        return appMapper.toResponseDto(appService.createApp(appCreateDto, orgGuid, SecurityUtils.getPrincipalId()), SecurityUtils.getPrincipalId());
    }

    @PutMapping("/org/{orgGuid}/app/{guid}")
    @ApiOperation(value = "Update application")
    public AppResponseDto updateApp(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE) @PathVariable String orgGuid,
            @ApiParam(value = "GUID of application that needs to be updated", required = true, example = GUID_EXAMPLE) @PathVariable String guid,
            @ApiParam(value = "Application data", required = true) @Valid @RequestBody AppUpdateDto appUpdateDto) {
        return appMapper.toResponseDto(appService.updateApp(appUpdateDto, guid, SecurityUtils.getPrincipalId()), SecurityUtils.getPrincipalId());
    }

    @PutMapping("/org/{orgGuid}/app/{guid}/apikey")
    @ApiOperation(value = "Generate new api-key for application")
    public void regenerateApiKey(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE) @PathVariable String orgGuid,
            @ApiParam(value = "GUID of the application which api-key needs to be regenerated", required = true, example = GUID_EXAMPLE) @PathVariable final String guid) {
        appService.regenerateApiKey(guid, SecurityUtils.getPrincipalId());
    }

    @DeleteMapping("/org/{orgGuid}/app/{guid}")
    @ApiOperation(value = "Delete application")
    public void deleteApp(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE) @PathVariable String orgGuid,
            @ApiParam(value = "GUID of the application that needs to be deleted", required = true, example = GUID_EXAMPLE) @PathVariable String guid) {
        appService.deleteApp(guid, SecurityUtils.getPrincipalId());
    }

    @GetMapping("/org/{orgGuid}/app/{guid}/assign-roles")
    @ApiOperation(value = "Get application roles, that can be assigned to other users")
    public AppRole[] getOrgRolesToAssign(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE) @PathVariable String orgGuid,
            @ApiParam(value = "GUID of the application", required = true, example = GUID_EXAMPLE) @PathVariable String guid) {
        return appService.getAppRolesToAssign(orgGuid, guid, SecurityUtils.getPrincipalId());
    }

    @GetMapping("/org/{orgGuid}/app/{guid}/roles")
    @ApiOperation(value = "Get users of application")
    public List<UserRoleResponseDto> getAppUsers(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE) @PathVariable String orgGuid,
            @ApiParam(value = "GUID of application", required = true, example = GUID_EXAMPLE) @PathVariable String guid,
            @ApiParam(value = "Filter by first name, last name or email of user") @RequestParam(required = false) String search) {
        return userAppRoleMapper.toUserRoleResponseDto(appService.getAppUsers(search, orgGuid, guid, SecurityUtils.getPrincipalId()));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/org/{orgGuid}/app/{guid}/invite")
    @ApiOperation(value = "Invite user to application")
    public UserRoleResponseDto inviteUser(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE) @PathVariable String orgGuid,
            @ApiParam(value = "GUID of application", required = true, example = GUID_EXAMPLE) @PathVariable String guid,
            @ApiParam(value = "Application object that needs to be created", required = true) @Valid @RequestBody UserInviteDto userInviteDto) {
        return userAppRoleMapper.toUserRoleResponseDto(appService.inviteUser(userInviteDto, orgGuid, guid, SecurityUtils.getPrincipalId()));
    }

    @PutMapping("/org/{orgGuid}/app/{guid}/role")
    @ApiOperation(value = "Update user application role")
    public void updateUserAppRole(
            @ApiParam(value = "GUID of organization", required = true, example = GUID_EXAMPLE) @PathVariable String orgGuid,
            @ApiParam(value = "GUID of application", required = true, example = GUID_EXAMPLE) @PathVariable final String guid,
            @ApiParam(value = "User role data", required = true) @Valid @RequestBody UserRoleUpdateDto userRoleUpdateDto) {
        appService.updateUserAppRole(userRoleUpdateDto, guid, SecurityUtils.getPrincipalId());
    }

}
