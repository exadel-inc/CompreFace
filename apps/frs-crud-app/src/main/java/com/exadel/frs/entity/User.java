package com.exadel.frs.entity;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NamedEntityGraph(
        name = "by-name",
        attributeNodes = {
                @NamedAttributeNode("userAppRoles"),
                @NamedAttributeNode("userOrganizationRoles")
        }
)
@Entity
@Table(schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id", "email", "guid"})
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
    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Set<UserAppRole> userAppRoles = new HashSet<>();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Set<UserOrganizationRole> userOrganizationRoles = new HashSet<>();

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
}
