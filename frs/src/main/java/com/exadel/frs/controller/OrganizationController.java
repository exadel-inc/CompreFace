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
    private final SecurityUtils securityUtils;

    @GetMapping("/{id}")
    @ApiOperation(value = "Get Organization, created by user")
    public OrganizationDto getOrganization(@ApiParam(value = "ID of Organization to return", required = true, example = "0") @PathVariable Long id) {
        return organizationMapper.toDto(organizationService.getOrganization(id, securityUtils.getPrincipal().getId()));
    }

    @GetMapping("/")
    @ApiOperation(value = "Get all Organizations, created by user")
    public List<OrganizationDto> getOrganizations() {
        return organizationMapper.toDto(organizationService.getOrganizations(securityUtils.getPrincipal().getId()));
    }

    @PostMapping("/")
    @ApiOperation(value = "Create Organization")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Organization name is required")
    })
    public void createOrganization(@ApiParam(value = "Organization object that needs to be created", required = true) @Valid @RequestBody OrganizationDto organizationDto) {
        organizationService.createOrganization(organizationMapper.toEntity(organizationDto), securityUtils.getPrincipal());
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Update Organization")
    public void updateOrganization(@ApiParam(value = "ID of Organization that needs to be updated", required = true, example = "0") @PathVariable Long id,
                                   @ApiParam(value = "Organization data", required = true) @Valid @RequestBody OrganizationDto organizationDto) {
        organizationService.updateOrganization(id, organizationMapper.toEntity(organizationDto), securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}/add-users")
    @ApiOperation(value = "Add users to Organization")
    public void addUsersToOrganization(@ApiParam(value = "ID of Organization that needs to be updated", required = true, example = "0") @PathVariable Long id,
                                       @ApiParam(value = "Organization data", required = true) @Valid @RequestBody OrganizationDto organizationDto) {
        organizationService.addUserToOrganization(id, organizationMapper.toEntity(organizationDto), securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}/remove-users")
    @ApiOperation(value = "Remove users from Organization")
    public void removeUsersFromOrganization(@ApiParam(value = "ID of Organization that needs to be updated", required = true, example = "0") @PathVariable Long id,
                                            @ApiParam(value = "Organization data", required = true) @Valid @RequestBody OrganizationDto organizationDto) {
        organizationService.removeUserFromOrganization(id, organizationMapper.toEntity(organizationDto), securityUtils.getPrincipal().getId());
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete Organization")
    public void deleteOrganization(@ApiParam(value = "ID of the Organization that needs to be deleted", required = true, example = "0") @PathVariable Long id) {
        organizationService.deleteOrganization(id, securityUtils.getPrincipal().getId());
    }

}
