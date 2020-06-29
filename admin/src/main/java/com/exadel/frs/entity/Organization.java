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

package com.exadel.frs.entity;

import static com.exadel.frs.enums.OrganizationRole.OWNER;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.UserDoesNotBelongToOrganization;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.val;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"guid"})
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organization_id_seq")
    @SequenceGenerator(name = "organization_id_seq", sequenceName = "organization_id_seq", allocationSize = 1)
    private Long id;
    private String name;
    private String guid;
    private Boolean isDefault;

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserOrganizationRole> userOrganizationRoles = new ArrayList<>();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<App> apps = new ArrayList<>();

    public Optional<UserOrganizationRole> getUserOrganizationRole(Long userId) {
        return userOrganizationRoles
                .stream()
                .filter(userOrganizationRole -> userOrganizationRole.getId().getUserId().equals(userId))
                .findFirst();
    }

    public UserOrganizationRole getUserOrganizationRoleOrThrow(Long userId) {
        return getUserOrganizationRole(userId)
                .orElseThrow(UserDoesNotBelongToOrganization::new);
    }

    public void addUserOrganizationRole(User user, OrganizationRole role) {
        val userOrganizationRole = new UserOrganizationRole(user, this, role);
        userOrganizationRoles.add(userOrganizationRole);
        user.getUserOrganizationRoles().add(userOrganizationRole);
    }

    public User getOwner() {
        return this.getUserOrganizationRoles().stream()
                                              .filter(orgRole -> orgRole.getRole() == OWNER)
                                              .findAny()
                                              .orElseThrow(IllegalStateException::new)
                                              .getUser();
    }
}