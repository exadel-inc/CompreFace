package com.exadel.frs.controller;

import com.exadel.frs.dto.AppDto;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.mapper.AppMapper;
import com.exadel.frs.service.AppService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/apps")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;
    private final SecurityUtils securityUtils;
    private final AppMapper appMapper;

    @GetMapping("/{id}")
    @ApiOperation(value = "Get application, created by user")
    public AppDto getApp(@ApiParam(value = "ID of application to return", required = true, example = "0") @PathVariable Long id) {
        return appMapper.toDto(appService.getApp(id, securityUtils.getPrincipal().getId()));
    }

    @GetMapping("/org/{organizationId}")
    @ApiOperation(value = "Get all applications, created by user")
    public List<AppDto> getApps(@PathVariable Long organizationId) {
        return appMapper.toDto(appService.getApps(organizationId, securityUtils.getPrincipal().getId()));
    }

    @PostMapping("/")
    @ApiOperation(value = "Create application")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Application name is required")
    })
    public void createApp(@ApiParam(value = "Application object that needs to be created", required = true) @Valid @RequestBody AppDto appDto) {
        appService.createApp(appMapper.toEntity(appDto), securityUtils.getPrincipal());
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Update application")
    public void updateApp(@ApiParam(value = "ID of application that needs to be updated", required = true, example = "0") @PathVariable Long id,
                          @ApiParam(value = "Application data", required = true) @Valid @RequestBody AppDto appDto) {
        appService.updateApp(id, appMapper.toEntity(appDto), securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}/guid")
    @ApiOperation(value = "Generate new GUID for application")
    public void regenerateGuid(@ApiParam(value = "ID of the application which GUID needs to be regenerated", required = true, example = "0") @PathVariable Long id) {
        appService.regenerateGuid(id, securityUtils.getPrincipal().getId());
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete application")
    public void deleteApp(@ApiParam(value = "ID of the application that needs to be deleted", required = true, example = "0") @PathVariable Long id) {
        appService.deleteApp(id, securityUtils.getPrincipal().getId());
    }

}
