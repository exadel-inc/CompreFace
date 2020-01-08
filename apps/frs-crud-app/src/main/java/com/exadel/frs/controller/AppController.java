package com.exadel.frs.controller;

import com.exadel.frs.dto.ui.AppCreateDto;
import com.exadel.frs.dto.ui.AppResponseDto;
import com.exadel.frs.dto.ui.AppUpdateDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.mapper.AppMapper;
import com.exadel.frs.service.AppService;
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

    @GetMapping("/app/{guid}")
    @ApiOperation(value = "Get application")
    public AppResponseDto getApp(@ApiParam(value = "GUID of application to return",
                                   required = true,
                                   example = GUID_EXAMPLE) @PathVariable String guid) {
        return appMapper.toResponseDto(appService.getApp(guid, SecurityUtils.getPrincipalId()));
    }

    @GetMapping("/org/{organizationGuid}/apps")
    @ApiOperation(value = "Get all applications in organization")
    public List<AppResponseDto> getApps(@PathVariable String organizationGuid) {
        return appMapper.toResponseDto(appService.getApps(organizationGuid, SecurityUtils.getPrincipalId()));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/org/{organizationGuid}/app")
    @ApiOperation(value = "Create application")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Application name is required")
    })
    public AppResponseDto createApp(@ApiParam(value = "GUID of organization",
                                    required = true,
                                    example = GUID_EXAMPLE) @PathVariable String organizationGuid,
                          @ApiParam(value = "Application object that needs to be created", required = true) @Valid @RequestBody AppCreateDto appCreateDto) {
        App app = appService.createApp(organizationGuid, appMapper.toEntity(appCreateDto), SecurityUtils.getPrincipalId());
        return appMapper.toResponseDto(app);
    }

    @PutMapping("/app/{guid}")
    @ApiOperation(value = "Update application")
    public AppResponseDto updateApp(@ApiParam(value = "GUID of application that needs to be updated", required = true, example = GUID_EXAMPLE) @PathVariable String guid,
                          @ApiParam(value = "Application data", required = true) @Valid @RequestBody AppUpdateDto appUpdateDto) {
        App app = appService.updateApp(guid, appMapper.toEntity(appUpdateDto), SecurityUtils.getPrincipalId());
        return appMapper.toResponseDto(app);
    }

    @PutMapping("/app/{guid}/api-key")
    @ApiOperation(value = "Generate new api-key for application")
    public void regenerateApiKey(@ApiParam(value = "GUID of the application which api-key needs to be regenerated",
                                           required = true,
                                           example = GUID_EXAMPLE) @PathVariable final String guid) {
        appService.regenerateApiKey(guid, SecurityUtils.getPrincipalId());
    }

    @DeleteMapping("/app/{guid}")
    @ApiOperation(value = "Delete application")
    public void deleteApp(@ApiParam(value = "GUID of the application that needs to be deleted",
                                    required = true,
                                    example = GUID_EXAMPLE) @PathVariable String guid) {
        appService.deleteApp(guid, SecurityUtils.getPrincipalId());
    }

}
