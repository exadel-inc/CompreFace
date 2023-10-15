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
import com.exadel.frs.commonservice.dto.PluginsVersionsDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@JsonInclude(NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class VerifyFacesResponse extends FaceProcessResponse {

    @JsonProperty("source_image_face")
    VerifyFacesResultDto processFileData;
    @JsonProperty("face_matches")
    List<FaceMatch> faceMatches;
    @JsonProperty("plugins_versions")
    PluginsVersionsDto pluginsVersions;

    @Override
    public VerifyFacesResponse prepareResponse(ProcessImageParams processImageParams) {
        String facePlugins = processImageParams.getFacePlugins();
        if (isEmpty(facePlugins) || !facePlugins.contains(CALCULATOR)) {
            this.getProcessFileData().setEmbedding(null);
            this.faceMatches.forEach(fm -> fm.setEmbedding(null));
        }

        if (Boolean.FALSE.equals(processImageParams.getStatus())) {
            this.getProcessFileData().setExecutionTime(null);
            this.faceMatches.forEach(fm -> fm.setExecutionTime(null));
            this.setPluginsVersions(null);
        }

        return this;
    }
}
