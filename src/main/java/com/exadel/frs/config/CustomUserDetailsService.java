package com.exadel.frs.config;

import com.exadel.frs.entity.User;
import com.exadel.frs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@RequiredArgsConstructor
@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("User " + username + " does not exists"));
        Hibernate.initialize(user.getUserOrganizationRoles());
        Hibernate.initialize(user.getUserAppRoles());
        return user;
    }

}
