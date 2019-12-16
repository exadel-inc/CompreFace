package com.exadel.frs.entity;

import com.exadel.frs.enums.AppRole;
import com.exadel.frs.helpers.AppRoleConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAppRole {

    @EmbeddedId
    private UserAppRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
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
