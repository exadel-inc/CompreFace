package com.exadel.frs.repository;

import com.exadel.frs.commonservice.entity.ResetPasswordToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, UUID> {

    void deleteByUserEmail(String email);

    Optional<ResetPasswordToken> findByUserEmail(String email);
}
