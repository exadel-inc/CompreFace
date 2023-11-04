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

package com.exadel.frs.commonservice.sdk.faces.feign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class FacesStatusResponse {

    @JsonProperty(value = "build_version")
    private String buildVersion1;

    @JsonProperty(value = "calculator_version")
    private String calculatorVersion;

    @JsonProperty(value = "status")
    private String status;

    @JsonProperty(value = "similarity_coefficients")
    private List<Double> similarityCoefficients;

    @JsonProperty(value = "available_plugins")
    private Map<String, String> availablePlugins;
}
