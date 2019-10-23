package com.exadel.frs.controller;

import com.exadel.frs.dto.AppDto;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.service.AppService;
import io.swagger.annotations.ApiOperation;
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

    @GetMapping("/{id}")
    @ApiOperation(value = "Get application, created by client")
    public AppDto getApp(@PathVariable Long id) {
        return appService.getApp(id, securityUtils.getPrincipal().getId());
    }

    @GetMapping("/")
    @ApiOperation(value = "Get all applications, created by client")
    public List<AppDto> getApps() {
        return appService.getApps(securityUtils.getPrincipal().getId());
    }

    @PostMapping("/")
    @ApiOperation(value = "Create application")
    public void createApp(@Valid @RequestBody AppDto inputAppDto) {
        appService.createApp(inputAppDto, securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Update application")
    public void updateApp(@PathVariable Long id, @Valid @RequestBody AppDto inputAppDto) {
        appService.updateApp(id, inputAppDto, securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}/guid")
    @ApiOperation(value = "Generate new GUID for application")
    public void regenerateGuid(@PathVariable Long id) {
        appService.regenerateGuid(id, securityUtils.getPrincipal().getId());
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete application")
    public void deleteApp(@PathVariable Long id) {
        appService.deleteApp(id, securityUtils.getPrincipal().getId());
    }

}
