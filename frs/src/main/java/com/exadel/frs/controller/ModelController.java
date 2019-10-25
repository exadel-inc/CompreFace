package com.exadel.frs.controller;

import com.exadel.frs.dto.ModelDto;
import com.exadel.frs.helpers.AccessUpdateType;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.service.ModelService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
    public ModelDto getModel(@ApiParam(value = "ID of model to return", required = true, example = "0") @PathVariable Long id) {
        return modelService.getModel(id, securityUtils.getPrincipal().getId());
    }

    @GetMapping("/")
    @ApiOperation(value = "Get all models, created by client")
    public List<ModelDto> getModels() {
        return modelService.getModels(securityUtils.getPrincipal().getId());
    }

    @PostMapping("/")
    @ApiOperation(value = "Create model")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Model name is required | Application access type to model is not correct")
    })
    public void createModel(@ApiParam(value = "Model object that needs to be created", required = true) @Valid @RequestBody ModelDto inputModelDto) {
        modelService.createModel(inputModelDto, securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Update model data")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Application access type to model is not correct")
    })
    public void updateModel(@ApiParam(value = "ID of model that needs to be updated", required = true, example = "0") @PathVariable Long id,
                            @ApiParam(value = "Model data", required = true) @Valid @RequestBody ModelDto inputModelDto) {
        modelService.updateModel(id, inputModelDto, securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}/grant-access")
    @ApiOperation(value = "Grant access to model for applications")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Application access type to model is not correct")
    })
    public void grantPrivileges(@ApiParam(value = "ID of model that needs to be updated", required = true, example = "0") @PathVariable Long id,
                                @ApiParam(value = "Privileges, that will be granted for applications", required = true) @Valid @RequestBody ModelDto inputModelDto) {
        modelService.updatePrivileges(id, inputModelDto, AccessUpdateType.ADD, securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}/remove-access")
    @ApiOperation(value = "Remove access to model for applications")
    public void removePrivileges(@ApiParam(value = "ID of model that needs to be updated", required = true, example = "0") @PathVariable Long id,
                                 @ApiParam(value = "Privileges, that will be removed for applications", required = true) @Valid @RequestBody ModelDto inputModelDto) {
        modelService.updatePrivileges(id, inputModelDto, AccessUpdateType.REMOVE, securityUtils.getPrincipal().getId());
    }

    @PutMapping("/{id}/guid")
    @ApiOperation(value = "Generate new GUID for model")
    public void regenerateGuid(@ApiParam(value = "ID of the model which GUID needs to be regenerated", required = true, example = "0") @PathVariable Long id) {
        modelService.regenerateGuid(id, securityUtils.getPrincipal().getId());
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete model")
    public void deleteModel(@ApiParam(value = "ID of the model that needs to be deleted", required = true, example = "0") @PathVariable Long id) {
        modelService.deleteModel(id, securityUtils.getPrincipal().getId());
    }

}
