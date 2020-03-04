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
@EqualsAndHashCode(of = {"guid"})
public class App {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_id_seq")
    @SequenceGenerator(name = "app_id_seq", sequenceName = "app_id_seq", allocationSize = 1)
    private Long id;
    private String name;
    private String guid;
    private String apiKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Organization organization;

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAppRole> userAppRoles = new ArrayList<>();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppModel> appModelAccess = new ArrayList<>();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Model> models = new ArrayList<>();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelShareRequest> modelShareRequests = new ArrayList<>();

    public Optional<UserAppRole> getOwner() {
        return userAppRoles
                .stream()
                .filter(userAppRole -> AppRole.OWNER.equals(userAppRole.getRole()))
                .findFirst();
    }

    public Optional<UserAppRole> getUserAppRole(Long userId) {
        return userAppRoles
                .stream()
                .filter(userAppRole -> userAppRole.getId().getUserId().equals(userId))
                .findFirst();
    }

    public void addUserAppRole(User user, AppRole role) {
        UserAppRole userAppRole = new UserAppRole(user, this, role);
        userAppRoles.add(userAppRole);
        user.getUserAppRoles().add(userAppRole);
    }
}
