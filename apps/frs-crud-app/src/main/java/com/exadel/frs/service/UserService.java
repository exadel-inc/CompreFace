package com.exadel.frs.service;

import com.exadel.frs.dto.ui.UserCreateDto;
import com.exadel.frs.dto.ui.UserUpdateDto;
import com.exadel.frs.entity.User;
import com.exadel.frs.exception.EmailAlreadyRegisteredException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.UserDoesNotExistException;
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

    public User getUser(final Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserDoesNotExistException(id.toString()));
    }

    public User getUser(final String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserDoesNotExistException(email));
    }

    public User getUserByGuid(final String guid) {
        return userRepository.findByGuid(guid)
                .orElseThrow(() -> new UserDoesNotExistException(guid));
    }

    public User createUser(final UserCreateDto userCreateDto) {
        if (StringUtils.isEmpty(userCreateDto.getEmail())) {
            throw new EmptyRequiredFieldException("email");
        }
        if (StringUtils.isEmpty(userCreateDto.getPassword())) {
            throw new EmptyRequiredFieldException("password");
        }
        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            throw new EmailAlreadyRegisteredException();
        }
        User user = User.builder()
                .email(userCreateDto.getEmail())
                .firstName(userCreateDto.getFirstName())
                .lastName(userCreateDto.getLastName())
                .password(encoder.encode(userCreateDto.getPassword()))
                .guid(UUID.randomUUID().toString())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    public void updateUser(final UserUpdateDto userUpdateDto, final Long userId) {
        User user = getUser(userId);
        if (!StringUtils.isEmpty(userUpdateDto.getFirstName())) {
            user.setFirstName(userUpdateDto.getFirstName());
        }
        if (!StringUtils.isEmpty(userUpdateDto.getLastName())) {
            user.setLastName(userUpdateDto.getLastName());
        }
        if (!StringUtils.isEmpty(userUpdateDto.getPassword())) {
            user.setPassword(encoder.encode(userUpdateDto.getPassword()));
        }
        userRepository.save(user);
    }

    public void deleteUser(final Long id) {
        userRepository.deleteById(id);
    }

}
