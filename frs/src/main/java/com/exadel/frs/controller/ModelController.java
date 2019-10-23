package com.exadel.frs.controller;

import com.exadel.frs.dto.ModelDto;
import com.exadel.frs.helpers.AccessUpdateType;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.service.ModelService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;
    private final SecurityUtils securityUtils;

    @GetMapping("/{id}")
    @ApiOperation(value = "Get model, created by client")
    public ModelDto getModel(@PathVariable Long id) {
        return modelService.getModel(id, securityUtils.getPrincipal().getId());
    }

    @GetMapping("/")
    @ApiOperation(value = "Get all models, created by client")
    public List<ModelDto> getModels() {
        return modelService.getModels(securityUtils.getPrincipal().getId());
    }

    @PostMapping("/")
    @ApiOperation(value = "Create model")
    public void createModel(@Valid @RequestBody ModelDto inputModelDto) {
        modelService.createModel(inputModelDto, securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Update model data")
    public void updateModel(@PathVariable Long id, @Valid @RequestBody ModelDto inputModelDto) {
        modelService.updateModel(id, inputModelDto, securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}/grant-access")
    @ApiOperation(value = "Grant access to model for applications")
    public void grantPrivileges(@PathVariable Long id, @Valid @RequestBody ModelDto inputModelDto) {
        modelService.updatePrivileges(id, inputModelDto, AccessUpdateType.ADD, securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}/remove-access")
    @ApiOperation(value = "Remove access to model for applications")
    public void removePrivileges(@PathVariable Long id, @Valid @RequestBody ModelDto inputModelDto) {
        modelService.updatePrivileges(id, inputModelDto, AccessUpdateType.REMOVE, securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}/guid")
    @ApiOperation(value = "Generate new GUID for model")
    public void regenerateGuid(@PathVariable Long id) {
        modelService.regenerateGuid(id, securityUtils.getPrincipal().getId());
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete model")
    public void deleteModel(@PathVariable Long id) {
        modelService.deleteModel(id, securityUtils.getPrincipal().getId());
    }

}
