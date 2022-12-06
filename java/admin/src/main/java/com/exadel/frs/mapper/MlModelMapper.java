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

package com.exadel.frs.mapper;

import com.exadel.frs.commonservice.entity.Model;
import com.exadel.frs.commonservice.projection.ModelProjection;
import com.exadel.frs.dto.ModelResponseDto;
import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MlModelMapper {

    @Mapping(source = "guid", target = "id")
    ModelResponseDto toResponseDto(Model model, @Context String appGuid);

    @Mapping(source = "guid", target = "id")
    ModelResponseDto toResponseDto(ModelProjection model, @Context String appGuid);

    List<ModelResponseDto> toResponseDto(List<Model> model, @Context String appGuid);
}
