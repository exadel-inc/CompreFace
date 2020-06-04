package com.exadel.frs.entity;

import com.exadel.frs.enums.AppRole;
import com.exadel.frs.helpers.AppRoleConverter;
import lombok.*;

import javax.persistence.*;

@Entity
@Table
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
