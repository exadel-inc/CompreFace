/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.controller;

import static com.exadel.frs.system.global.Constants.GUID_EXAMPLE;
import static org.springframework.http.HttpStatus.CREATED;
import com.exadel.frs.dto.ui.ModelCreateDto;
import com.exadel.frs.dto.ui.ModelResponseDto;
import com.exadel.frs.dto.ui.ModelShareDto;
import com.exadel.frs.dto.ui.ModelUpdateDto;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.mapper.MlModelMapper;
import com.exadel.frs.service.ModelService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/{appGuid}")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;
    private final MlModelMapper modelMapper;

    @GetMapping("/model/{guid}")
    @ApiOperation(value = "Get model")
    public ModelResponseDto getModel(
            @ApiParam(value = "GUID of application", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String appGuid,
            @ApiParam(value = "GUID of model to return", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String guid
    ) {
        return modelMapper.toResponseDto(
                modelService.getModel(appGuid, guid, SecurityUtils.getPrincipalId()),
                appGuid
        );
    }

    @GetMapping("/models")
    @ApiOperation(value = "Get all models in application")
    public List<ModelResponseDto> getModels(
            @ApiParam(value = "GUID of application", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String appGuid
    ) {
        return modelMapper.toResponseDto(
                modelService.getModels(appGuid, SecurityUtils.getPrincipalId()),
                appGuid
        );
    }

    @ResponseStatus(CREATED)
    @PostMapping("/model")
    @ApiOperation(value = "Create model")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Field name cannot be empty | Application access type to model is not correct")
    })
    public ModelResponseDto createModel(
            @ApiParam(value = "GUID of application", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String appGuid,
            @ApiParam(value = "Model object that needs to be created", required = true)
            @Valid
            @RequestBody
            final ModelCreateDto modelCreateDto
    ) {
        return modelMapper.toResponseDto(
                modelService.createModel(modelCreateDto, appGuid, SecurityUtils.getPrincipalId()), appGuid
        );
    }

    @PutMapping("/model/{guid}")
    @ApiOperation(value = "Update model data")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Field name cannot be empty | Application access type to model is not correct")
    })
    public ModelResponseDto updateModel(
            @ApiParam(value = "GUID of application", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String appGuid,
            @ApiParam(value = "GUID of model that needs to be updated", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String guid,
            @ApiParam(value = "Model data", required = true)
            @Valid
            @RequestBody
            final ModelUpdateDto modelUpdateDto
    ) {
        var updatedModel = modelService.updateModel(modelUpdateDto, appGuid, guid, SecurityUtils.getPrincipalId());

        return modelMapper.toResponseDto(updatedModel, appGuid);
    }

    @PutMapping("/model/{guid}/apikey")
    @ApiOperation(value = "Generate new api-key for model")
    public ModelResponseDto regenerateApiKey(
            @ApiParam(value = "GUID of application", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String appGuid,
            @ApiParam(value = "GUID of the model which GUID needs to be regenerated", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String guid
    ) {
        modelService.regenerateApiKey(appGuid, guid, SecurityUtils.getPrincipalId());

        return modelMapper.toResponseDto(modelService.getModel(appGuid, guid, SecurityUtils.getPrincipalId()), appGuid);
    }

    @DeleteMapping("/model/{guid}")
    @ApiOperation(value = "Delete model")
    public void deleteModel(
            @ApiParam(value = "GUID of application", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String appGuid,
            @ApiParam(value = "GUID of the model that needs to be deleted", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String guid
    ) {
        modelService.deleteModel(appGuid, guid, SecurityUtils.getPrincipalId());
    }

    @PostMapping("/model/{guid}/share")
    @ApiOperation("Sharing model with another application")
    public void shareModel(
            @ApiParam(value = "GUID of application", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String appGuid,
            @ApiParam(value = "GUID of the model being shared", required = true, example = GUID_EXAMPLE)
            @PathVariable
            final String guid,
            @ApiParam(value = "GUID from model share request", required = true)
            @RequestBody
            final ModelShareDto modelShare
    ) {
        modelService.share(modelShare, appGuid, guid);
    }
}