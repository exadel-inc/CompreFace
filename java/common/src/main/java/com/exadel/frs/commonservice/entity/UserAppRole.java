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

package com.exadel.frs.commonservice.entity;

import com.exadel.frs.commonservice.enums.AppRole;
import com.exadel.frs.commonservice.helpers.AppRoleConverter;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "user_app_role", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"user", "app"})
public class UserAppRole {

    @EmbeddedId
    private UserAppRoleId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("appId")
    private App app;

    @Convert(converter = AppRoleConverter.class)
    private AppRole role;

    public UserAppRole(User user, App app, AppRole role) {
        this.user = user;
        this.app = app;
        this.role = role;
        this.id = new UserAppRoleId(user.getId(), app.getId());
    }
}
