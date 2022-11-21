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

package com.exadel.frs.dto.ui;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import com.exadel.frs.commonservice.enums.ModelType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@JsonInclude(NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelResponseDto {

    private String id;
    private String name;
    private String apiKey;
    private ModelType type;
    private Long subjectCount;
    private Long imageCount;
    private LocalDateTime createdDate;
}
