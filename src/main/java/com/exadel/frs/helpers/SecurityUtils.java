package com.exadel.frs.helpers;

import com.exadel.frs.entity.User;
import com.exadel.frs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public final class SecurityUtils {

    private final UserRepository userRepository;

    public User getPrincipal() {
        // todo: get Id from authentication and replace clientRepository.findByUsername method
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(principal.toString())
                .orElseThrow(() -> new UsernameNotFoundException("User " + principal.toString() + " does not exists"));
    }

}
