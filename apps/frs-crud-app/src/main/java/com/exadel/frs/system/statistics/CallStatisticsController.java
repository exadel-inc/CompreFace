package com.exadel.frs.system.statistics;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistics/calls")
@RequiredArgsConstructor
public class CallStatisticsController {
    private final CallStatisticsService service;

    @GetMapping
    @ApiOperation(value = "Get all info about call count to models and apps")
    public Page<CallStatisticsInfo> getAll(@ApiParam(value = "Parameters to build page for response") Pageable pageable) {
        return service.findAll(pageable);
    }
}
