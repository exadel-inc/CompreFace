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

    @GetMapping("/{guid}")
    @ApiOperation(value = "Get model")
    public ModelDto getModel(@ApiParam(value = "GUID of model to return",
                                       required = true,
                                       example = "0") @PathVariable final String guid) {
        return modelMapper.toDto(modelService.getModel(guid, SecurityUtils.getPrincipalId()));
    }

    @GetMapping("/app/{appGuid}")
    @ApiOperation(value = "Get all models in application")
    public List<ModelDto> getModels(@PathVariable final String appGuid) {
        return modelMapper.toDto(modelService.getModels(appGuid, SecurityUtils.getPrincipalId()));
    }

    @PostMapping("/")
    @ApiOperation(value = "Create model")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Model name is required | Application access type to model is not correct")
    })
    public void createModel(@ApiParam(value = "Model object that needs to be created", required = true) @Valid @RequestBody ModelDto modelDto) {
        modelService.createModel(modelMapper.toEntity(modelDto), SecurityUtils.getPrincipalId());
    }

    @PutMapping("/{guid}")
    @ApiOperation(value = "Update model data")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Application access type to model is not correct")
    })
    public void updateModel(@ApiParam(value = "GUID of model that needs to be updated",
                                      required = true,
                                      example = "0") @PathVariable final String guid,
                            @ApiParam(value = "Model data", required = true) @Valid @RequestBody ModelDto modelDto) {
        modelService.updateModel(guid, modelMapper.toEntity(modelDto), SecurityUtils.getPrincipalId());
    }

    @PutMapping("/{guid}/api-key")
    @ApiOperation(value = "Generate new api-key for model")
    public void regenerateApiKey(@ApiParam(value = "GUID of the model which GUID needs to be regenerated",
                                         required = true,
                                         example = "0") @PathVariable final String guid) {
        modelService.regenerateApiKey(guid, SecurityUtils.getPrincipalId());
    }

    @DeleteMapping("/{guid}")
    @ApiOperation(value = "Delete model")
    public void deleteModel(@ApiParam(value = "GUID of the model that needs to be deleted",
                                      required = true,
                                      example = "0") @PathVariable final String guid) {
        modelService.deleteModel(guid, SecurityUtils.getPrincipalId());
    }

}
