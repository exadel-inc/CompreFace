package com.exadel.frs.controller;

import com.exadel.frs.dto.AppDto;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.service.AppService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import javax.validation.Valid;

@RestController
@RequestMapping("/apps")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;
    private final SecurityUtils securityUtils;

    @GetMapping("/{id}")
    @ApiOperation(value = "app by id")
    public AppDto getApp(@PathVariable Long id) {
        return appService.getApp(id, securityUtils.getPrincipal().getId());
    }

    @GetMapping("/")
    public List<AppDto> getApps() {
        return appService.getApps(securityUtils.getPrincipal().getId());
    }

    @PostMapping("/")
    public void createApp(@Valid @RequestBody AppDto inputAppDto) {
        appService.createApp(inputAppDto, securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}")
    public void updateApp(@PathVariable Long id, @Valid @RequestBody AppDto inputAppDto) {
        appService.updateApp(id, inputAppDto, securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}/guid")
    public void regenerateGuid(@PathVariable Long id) {
        appService.regenerateGuid(id, securityUtils.getPrincipal().getId());
    }

    @DeleteMapping("/{id}")
    public void deleteApp(@PathVariable Long id) {
        appService.deleteApp(id, securityUtils.getPrincipal().getId());
    }

}
