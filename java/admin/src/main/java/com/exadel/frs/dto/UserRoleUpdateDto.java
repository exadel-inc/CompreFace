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

import com.exadel.frs.commonservice.enums.GlobalRole;
import com.exadel.frs.validation.ValidEnum;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleUpdateDto {

    @NotBlank(message = "UserId cannot be empty")
    private String userId;

    @NotBlank(message = "Role cannot be empty")
    @ValidEnum(message = "Global role '${validatedValue}' doesn't exist!", targetClassType = GlobalRole.class)
    private String role;
}
