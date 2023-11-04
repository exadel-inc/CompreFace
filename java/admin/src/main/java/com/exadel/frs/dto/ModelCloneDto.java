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

package com.exadel.frs.dto;

import static com.exadel.frs.commonservice.system.global.RegExConstants.ALLOWED_SPECIAL_CHARACTERS;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelCloneDto {

    @NotBlank(message = "Model name cannot be empty")
    @Size(min = 1, max = 50, message = "Model name size must be between 1 and 50")
    @Pattern(regexp = ALLOWED_SPECIAL_CHARACTERS, message = "The name cannot contain the following special characters: ';', '/', '\\'")
    private String name;
}
