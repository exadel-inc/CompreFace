package com.exadel.frs.repository;

import com.exadel.frs.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(value = "by-name")
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
