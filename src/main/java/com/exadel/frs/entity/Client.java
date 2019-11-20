package com.exadel.frs.entity;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

@Entity
@Table
@Data
public class Client implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_id_seq")
    @SequenceGenerator(name = "client_id_seq", sequenceName = "client_id_seq", allocationSize = 1)
    private Long id;
    private String email;
    private String username;
    private String password;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    @OneToMany(mappedBy = "owner")
    private List<App> apps;

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

}
