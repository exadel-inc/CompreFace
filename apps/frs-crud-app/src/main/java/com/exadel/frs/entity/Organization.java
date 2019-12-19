package com.exadel.frs.entity;

import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.UserDoesNotBelongToOrganization;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organization_id_seq")
    @SequenceGenerator(name = "organization_id_seq", sequenceName = "organization_id_seq", allocationSize = 1)
    private Long id;
    private String name;
    private String guid;

    @ToString.Exclude
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserOrganizationRole> userOrganizationRoles;

    @ToString.Exclude
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<App> apps;

    public Optional<UserOrganizationRole> getUserOrganizationRole(Long userId) {
        if (userOrganizationRoles == null) {
            return Optional.empty();
        }
        return userOrganizationRoles
                .stream()
                .filter(userOrganizationRole -> userOrganizationRole.getId().getUserId().equals(userId))
                .findFirst();
    }

    public UserOrganizationRole getUserOrganizationRoleOrThrow(Long userId) {
        return getUserOrganizationRole(userId)
                .orElseThrow(() -> new UserDoesNotBelongToOrganization(userId, id));
    }

    public void addUserOrganizationRole(User user, OrganizationRole role) {
        if (userOrganizationRoles == null) {
            userOrganizationRoles = new ArrayList<>();
        }
        if (user.getUserOrganizationRoles() == null) {
            user.setUserOrganizationRoles(new ArrayList<>());
        }
        UserOrganizationRole userOrganizationRole = new UserOrganizationRole(user, this, role);
        userOrganizationRoles.add(userOrganizationRole);
        user.getUserOrganizationRoles().add(userOrganizationRole);
    }

}
