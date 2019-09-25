package com.exadel.frs.controller;

import com.exadel.frs.dto.AppDto;
import com.exadel.frs.service.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/apps")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

    @GetMapping("/{id}")
    public AppDto getApp(@PathVariable Long id) {
        return appService.getApp(id);
    }

    @GetMapping("/")
    public List<AppDto> getApps() {
        return appService.getApps();
    }

    @PostMapping("/")
    public void createApp(@Valid @RequestBody AppDto inputAppDto) {
        appService.createApp(inputAppDto);
    }

    @PutMapping("/{id}")
    public void updateApp(@PathVariable Long id, @Valid @RequestBody AppDto inputAppDto) {
        appService.updateApp(id, inputAppDto);
    }

    @PutMapping("/{id}/guid")
    public void regenerateGuid(@PathVariable Long id) {
        appService.regenerateGuid(id);
    }

    @DeleteMapping("/{id}")
    public void deleteApp(@PathVariable Long id) {
        appService.deleteApp(id);
    }

}
