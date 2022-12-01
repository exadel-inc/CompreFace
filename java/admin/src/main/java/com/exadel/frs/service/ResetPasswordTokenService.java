package com.exadel.frs.service;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MILLIS;
import static liquibase.repackaged.org.apache.commons.text.StringSubstitutor.replace;
import com.exadel.frs.commonservice.entity.ResetPasswordToken;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.exception.InvalidResetPasswordTokenException;
import com.exadel.frs.exception.MailServerDisabledException;
import com.exadel.frs.helpers.EmailSender;
import com.exadel.frs.repository.ResetPasswordTokenRepository;
import java.util.Map;
import java.util.UUID;
import liquibase.repackaged.org.apache.commons.text.StringSubstitutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResetPasswordTokenService {

    @Value("${forgot-password.reset-password-token.expires}")
    private long tokenExpires;

    private final ResetPasswordTokenRepository tokenRepository;
    private final UserService userService;
    private final EmailSender emailSender;
    private final Environment env;

    @Transactional
    @Scheduled(cron = "${forgot-password.cleaner.scheduler.cron}", zone = "UTC")
    public void deleteExpiredTokens() {
        tokenRepository.deleteAllByExpiresInBefore(now(UTC));
    }

    @Transactional
    public User exchangeTokenOnUser(final String rawToken) {
        try {
            val token = UUID.fromString(rawToken);
            val resetPasswordToken = tokenRepository.findById(token)
                                                    .orElseThrow(InvalidResetPasswordTokenException::new);

            if (resetPasswordToken.getExpiresIn().isAfter(now(UTC))) {
                tokenRepository.delete(resetPasswordToken);
                return resetPasswordToken.getUser();
            } else {
                throw new InvalidResetPasswordTokenException();
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new InvalidResetPasswordTokenException();
        }
    }

    @Transactional
    public void assignAndSendToken(final String email) {
        val isMailServerEnabled = parseBoolean(env.getProperty("spring.mail.enable"));

        if (isMailServerEnabled) {
            val user = userService.getEnabledUserByEmail(email);
            val token = assignToken(user);

            sendToken(email, token);
        } else {
            log.warn("Cannot send an email due to the email server being disabled!");
            throw new MailServerDisabledException();
        }
    }

    private UUID assignToken(final User user) {
        val expiresIn = now(UTC).plus(tokenExpires, MILLIS);
        val resetPasswordToken = new ResetPasswordToken(expiresIn, user);

        tokenRepository.deleteByUserEmail(user.getEmail());
        tokenRepository.flush();
        tokenRepository.save(resetPasswordToken);

        return resetPasswordToken.getToken();
    }

    private void sendToken(final String email, final UUID token) {
        val messageParams = Map.of(
                "host", env.getProperty("host.frs"),
                "token", token.toString()
        );

        val message = StringSubstitutor.replace("""
                In order to reset a password click the link below:<br>
                <a href="${host}/reset-password?token=${token}">
                    ${host}/reset-password?token=${token}
                </a>
                """, messageParams, "${", "}");

        emailSender.sendMail(
                email,
                "CompreFace Reset Password",
                message
        );
    }
}
