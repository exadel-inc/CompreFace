package com.exadel.frs.controller;

import com.exadel.frs.dto.ModelDto;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.mapper.MlModelMapper;
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
    private final MlModelMapper modelMapper;

    @GetMapping("/{id}")
    @ApiOperation(value = "Get model, created by user")
    public ModelDto getModel(@ApiParam(value = "ID of model to return", required = true, example = "0") @PathVariable Long id) {
        return modelMapper.toDto(modelService.getModel(id, SecurityUtils.getPrincipalId()));
    }

    @GetMapping("/app/{appId}")
    @ApiOperation(value = "Get all models, created by user")
    public List<ModelDto> getModels(@PathVariable Long appId) {
        return modelMapper.toDto(modelService.getModels(appId, SecurityUtils.getPrincipalId()));
    }

    @PostMapping("/")
    @ApiOperation(value = "Create model")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Model name is required | Application access type to model is not correct")
    })
    public void createModel(@ApiParam(value = "Model object that needs to be created", required = true) @Valid @RequestBody ModelDto modelDto) {
        modelService.createModel(modelMapper.toEntity(modelDto), SecurityUtils.getPrincipalId());
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Update model data")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Application access type to model is not correct")
    })
    public void updateModel(@ApiParam(value = "ID of model that needs to be updated", required = true, example = "0") @PathVariable Long id,
                            @ApiParam(value = "Model data", required = true) @Valid @RequestBody ModelDto modelDto) {
        modelService.updateModel(id, modelMapper.toEntity(modelDto), SecurityUtils.getPrincipalId());
    }

    @PutMapping("/{id}/guid")
    @ApiOperation(value = "Generate new GUID for model")
    public void regenerateGuid(@ApiParam(value = "ID of the model which GUID needs to be regenerated", required = true, example = "0") @PathVariable Long id) {
        modelService.regenerateGuid(id, SecurityUtils.getPrincipalId());
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete model")
    public void deleteModel(@ApiParam(value = "ID of the model that needs to be deleted", required = true, example = "0") @PathVariable Long id) {
        modelService.deleteModel(id, SecurityUtils.getPrincipalId());
    }

}
