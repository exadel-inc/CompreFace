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
    private final AppMapper appMapper;

    @GetMapping("/{guid}")
    @ApiOperation(value = "Get application, created by user")
    public AppDto getApp(@ApiParam(value = "GUID of application to return",
                                   required = true,
                                   example = "3913717b-a40b-4d6f-acc4-a861aa612651") @PathVariable String guid) {
        return appMapper.toDto(appService.getApp(guid, SecurityUtils.getPrincipalId()));
    }

    @GetMapping("/org/{organizationGuid}")
    @ApiOperation(value = "Get all applications, created by user")
    public List<AppDto> getApps(@PathVariable String organizationGuid) {
        return appMapper.toDto(appService.getApps(organizationGuid, SecurityUtils.getPrincipalId()));
    }

    @PostMapping("/")
    @ApiOperation(value = "Create application")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Application name is required")
    })
    public void createApp(@ApiParam(value = "Application object that needs to be created", required = true) @Valid @RequestBody AppDto appDto) {
        appService.createApp(appMapper.toEntity(appDto), SecurityUtils.getPrincipalId());
    }

    @PutMapping("/{guid}")
    @ApiOperation(value = "Update application")
    public void updateApp(@ApiParam(value = "GUID of application that needs to be updated", required = true, example = "0") @PathVariable String guid,
                          @ApiParam(value = "Application data", required = true) @Valid @RequestBody AppDto appDto) {
        appService.updateApp(guid, appMapper.toEntity(appDto), SecurityUtils.getPrincipalId());
    }

    @PutMapping("/{guid}/api-key")
    @ApiOperation(value = "Generate new api-key for application")
    public void regenerateApiKey(@ApiParam(value = "GUID of the application which api-key needs to be regenerated",
                                           required = true,
                                           example = "0") @PathVariable final String guid) {
        appService.regenerateApiKey(guid, SecurityUtils.getPrincipalId());
    }

    @DeleteMapping("/{guid}")
    @ApiOperation(value = "Delete application")
    public void deleteApp(@ApiParam(value = "GUID of the application that needs to be deleted",
                                    required = true,
                                    example = "0") @PathVariable String guid) {
        appService.deleteApp(guid, SecurityUtils.getPrincipalId());
    }

}
