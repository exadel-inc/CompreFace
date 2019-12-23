package com.exadel.frs.entity;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    private Long id;
    private String email;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String guid;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    @ToString.Exclude
    @OneToMany(mappedBy = "user")
    private List<UserAppRole> userAppRoles;

    @ToString.Exclude
    @OneToMany(mappedBy = "user")
    private List<UserOrganizationRole> userOrganizationRoles;

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

}
