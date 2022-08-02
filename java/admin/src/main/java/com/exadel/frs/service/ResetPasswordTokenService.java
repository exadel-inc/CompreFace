package com.exadel.frs.service;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.UUID.randomUUID;
import com.exadel.frs.commonservice.entity.ResetPasswordToken;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.dto.ui.ResetPasswordTokenDto;
import com.exadel.frs.repository.ResetPasswordTokenRepository;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResetPasswordTokenService {

    @Value("${reset-password.token.expires}")
    private long tokenExpires;

    private final ResetPasswordTokenRepository tokenRepository;
    private final UserService userService;

    public void sendResetPasswordTokenToUser(final ResetPasswordToken token) {
        // TODO: needs to be implemented
    }

    @Transactional
    public ResetPasswordToken assignResetPasswordTokenToUser(final ResetPasswordTokenDto tokenDto) {
        val user = userService.getEnabledUserByEmail(tokenDto.getEmail());
        val token = generateResetPasswordToken(user);

        tokenRepository.deleteByUserEmail(tokenDto.getEmail());
        tokenRepository.save(token);

        return token;
    }

    private ResetPasswordToken generateResetPasswordToken(final User user) {
        return ResetPasswordToken.builder()
                                 .token(randomUUID())
                                 .expireIn(now(UTC).plus(tokenExpires, MILLIS))
                                 .user(user)
                                 .build();
    }
}
