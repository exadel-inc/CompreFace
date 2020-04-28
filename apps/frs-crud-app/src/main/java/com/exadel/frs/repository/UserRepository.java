package com.exadel.frs.repository;

import com.exadel.frs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndEnabledTrue(String email);

    Optional<User> findByGuid(String guid);

    boolean existsByEmail(String email);

    @Query("from User where email like :q or firstName like :q or lastName like :q")
    List<User> autocomplete(String q);

    int deleteByEnabledFalseAndRegTimeBefore(LocalDateTime time);

    Optional<User> findByRegistrationToken(String token);
}