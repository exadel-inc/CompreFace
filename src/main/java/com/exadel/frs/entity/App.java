package com.exadel.frs.entity;

import com.exadel.frs.enums.AppRole;
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
public class App {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_id_seq")
    @SequenceGenerator(name = "app_id_seq", sequenceName = "app_id_seq", allocationSize = 1)
    private Long id;
    private String name;
    private String guid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ToString.Exclude
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAppRole> userAppRoles;

    @ToString.Exclude
    @OneToMany(mappedBy = "app")
    private List<AppModel> appModelAccess;

    @ToString.Exclude
    @OneToMany(mappedBy = "app")
    private List<Model> models;

    public Optional<UserAppRole> getUserAppRole(Long userId) {
        if (userAppRoles == null) {
            return Optional.empty();
        }
        return userAppRoles
                .stream()
                .filter(userAppRole -> userAppRole.getId().getUserId().equals(userId))
                .findFirst();
    }

    public void addUserAppRole(User user, AppRole role) {
        if (userAppRoles == null) {
            userAppRoles = new ArrayList<>();
        }
        if (user.getUserAppRoles() == null) {
            user.setUserAppRoles(new ArrayList<>());
        }
        UserAppRole userAppRole = new UserAppRole(user, this, role);
        userAppRoles.add(userAppRole);
        user.getUserAppRoles().add(userAppRole);
    }

}
