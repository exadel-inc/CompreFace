package com.exadel.frs.service;

import com.exadel.frs.entity.User;
import com.exadel.frs.exception.EmailAlreadyRegisteredException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.UserDoesNotExistException;
import com.exadel.frs.exception.UsernameAlreadyExistException;
import com.exadel.frs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserDoesNotExistException(id));
    }

    public void createUser(User user) {
        if (StringUtils.isEmpty(user.getUsername())) {
            throw new EmptyRequiredFieldException("username");
        }
        if (StringUtils.isEmpty(user.getPassword())) {
            throw new EmptyRequiredFieldException("password");
        }
        if (StringUtils.isEmpty(user.getEmail())) {
            throw new EmptyRequiredFieldException("email");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistException();
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyRegisteredException();
        }
        user.setGuid(UUID.randomUUID().toString());
        user.setPassword(encoder.encode(user.getPassword()));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void updateUser(Long repoUserId, User user) {
        User repoUser = getUser(repoUserId);
        if (!repoUser.getEmail().equals(user.getEmail()) &&
                userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyRegisteredException();
        }
        if (!StringUtils.isEmpty(user.getFirstName())) {
            repoUser.setFirstName(user.getFirstName());
        }
        if (!StringUtils.isEmpty(user.getLastName())) {
            repoUser.setLastName(user.getLastName());
        }
        if (!StringUtils.isEmpty(user.getEmail())) {
            repoUser.setEmail(user.getEmail());
        }
        if (!StringUtils.isEmpty(user.getPassword())) {
            repoUser.setPassword(encoder.encode(user.getPassword()));
        }
        userRepository.save(repoUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

}
