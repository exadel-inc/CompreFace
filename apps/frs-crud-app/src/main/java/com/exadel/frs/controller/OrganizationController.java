package com.exadel.frs.controller;

import com.exadel.frs.dto.OrganizationDto;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.mapper.OrganizationMapper;
import com.exadel.frs.service.OrganizationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final OrganizationMapper organizationMapper;

    @GetMapping("/{guid}")
    @ApiOperation(value = "Get Organization")
    public OrganizationDto getOrganization(@ApiParam(value = "GUID of Organization to return",
                                                     required = true,
                                                     example = "0") @PathVariable final String guid) {
        return organizationMapper.toDto(organizationService.getOrganization(guid, SecurityUtils.getPrincipalId()));
    }

    @GetMapping("/")
    @ApiOperation(value = "Get all Organizations, the user is a member of")
    public List<OrganizationDto> getOrganizations() {
        return organizationMapper.toDto(organizationService.getOrganizations(SecurityUtils.getPrincipalId()));
    }

    @PostMapping("/")
    @ApiOperation(value = "Create Organization")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Organization name is required")
    })
    public void createOrganization(@ApiParam(value = "Organization object that needs to be created", required = true) @Valid @RequestBody OrganizationDto organizationDto) {
        organizationService.createOrganization(organizationMapper.toEntity(organizationDto), SecurityUtils.getPrincipalId());
    }

    @PutMapping("/{guid}")
    @ApiOperation(value = "Update Organization")
    public void updateOrganization(@ApiParam(value = "GUID of Organization that needs to be updated",
                                             required = true,
                                             example = "0") @PathVariable final String guid,
                                   @ApiParam(value = "Organization data", required = true) @Valid @RequestBody OrganizationDto organizationDto) {
        organizationService.updateOrganization(guid, organizationMapper.toEntity(organizationDto), SecurityUtils.getPrincipalId());
    }

    @PutMapping("/{guid}/add-users")
    @ApiOperation(value = "Add users to Organization")
    public void addUsersToOrganization(@ApiParam(value = "GUID of Organization that needs to be updated",
                                                 required = true,
                                                 example = "0") @PathVariable final String guid,
                                       @ApiParam(value = "Organization data", required = true) @Valid @RequestBody OrganizationDto organizationDto) {
        organizationService.addUserToOrganization(guid, organizationMapper.toEntity(organizationDto), SecurityUtils.getPrincipalId());
    }

    @PutMapping("/{guid}/remove-users")
    @ApiOperation(value = "Remove users from Organization")
    public void removeUsersFromOrganization(@ApiParam(value = "GUID of Organization that needs to be updated", required = true, example = "0") @PathVariable final String guid,
                                            @ApiParam(value = "Organization data", required = true) @Valid @RequestBody OrganizationDto organizationDto) {
        organizationService.removeUserFromOrganization(guid, organizationMapper.toEntity(organizationDto), SecurityUtils.getPrincipalId());
    }

    @DeleteMapping("/{guid}")
    @ApiOperation(value = "Delete Organization")
    public void deleteOrganization(@ApiParam(value = "GUID of the Organization that needs to be deleted", required = true, example = "0") @PathVariable final String guid) {
        organizationService.deleteOrganization(guid, SecurityUtils.getPrincipalId());
    }

}
