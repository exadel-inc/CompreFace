package com.exadel.frs.repository;

import com.exadel.frs.commonservice.entity.ResetPasswordToken;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, UUID> {

    @Modifying
    @Query("delete from ResetPasswordToken token where token.user.email = :email")
    void deleteByUserEmail(String email);
}
