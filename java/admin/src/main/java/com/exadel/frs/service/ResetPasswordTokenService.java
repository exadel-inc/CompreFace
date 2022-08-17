package com.exadel.frs.service;

import static java.lang.Boolean.parseBoolean;
import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.UUID.randomUUID;
import com.exadel.frs.commonservice.entity.ResetPasswordToken;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.exception.MailServerDisabledException;
import com.exadel.frs.helpers.EmailSender;
import com.exadel.frs.repository.ResetPasswordTokenRepository;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResetPasswordTokenService {

    @Value("${forgot-password.reset-password-token.expires}")
    private long tokenExpires;

    @Value("${forgot-password.email.subject}")
    private String emailSubject;

    @Value("${forgot-password.email.message-template}")
    private String emailMessageTemplate;

    private final ResetPasswordTokenRepository tokenRepository;
    private final UserService userService;
    private final EmailSender emailSender;
    private final Environment env;

    @Transactional
    public void assignAndSendToken(final String email) {
        val isMailServerEnabled = parseBoolean(env.getProperty("spring.mail.enable"));

        if (isMailServerEnabled) {
            val user = userService.getEnabledUserByEmail(email);
            val token = assignToken(user);

            sendToken(email, token);
        } else {
            throw new MailServerDisabledException();
        }
    }

    private UUID assignToken(final User user) {
        val token = buildToken(user);

        tokenRepository.deleteByUserEmail(user.getEmail());
        tokenRepository.flush();
        tokenRepository.save(token);

        return token.getToken();
    }

    private ResetPasswordToken buildToken(final User user) {
        return ResetPasswordToken.builder()
                                 .token(randomUUID())
                                 .expireIn(now(UTC).plus(tokenExpires, MILLIS))
                                 .user(user)
                                 .build();
    }

    private void sendToken(final String email, final UUID token) {
        val emailMessage = buildEmailMessage(token);
        emailSender.sendMail(email, emailSubject, emailMessage);
    }

    private String buildEmailMessage(final UUID token) {
        return String.format(emailMessageTemplate, env.getProperty("host.frs"), token.toString());
    }
}
