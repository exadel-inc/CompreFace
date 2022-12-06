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

package com.exadel.frs.core.trainservice.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import com.exadel.frs.commonservice.dto.ExecutionTimeDto;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesAge;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesBox;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesGender;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesMask;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesPose;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@JsonInclude(NON_NULL)
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class FaceVerification extends FaceProcessResponse {

    private FacesBox box;
    private String subject;
    private float similarity;
    private List<List<Integer>> landmarks;
    private FacesAge age;
    private FacesGender gender;
    private FacesPose pose;
    private Double[] embedding;
    @JsonProperty(value = "execution_time")
    private ExecutionTimeDto executionTime;
    private FacesMask mask;
    @Override
    public FaceVerification prepareResponse(ProcessImageParams processImageParams) {
        String facePlugins = processImageParams.getFacePlugins();
        if (isEmpty(facePlugins) || !facePlugins.contains(CALCULATOR)) {
            this.setEmbedding(null);
        }

        if (Boolean.FALSE.equals(processImageParams.getStatus())) {
            this.setExecutionTime(null);
        }

        return this;
    }
}
