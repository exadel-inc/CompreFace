package com.exadel.frs;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.exadel.frs.exception.UserDoesNotExistException;
import com.exadel.frs.repository.ResetPasswordTokenRepository;
import com.exadel.frs.service.ResetPasswordTokenService;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ResetPasswordTokenServiceTestIT extends EmbeddedPostgreSQLTest {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("compreface@exadel.com", "1234567890"))
            .withPerMethodLifecycle(true);

    @Value("${forgot-password.email.subject}")
    private String emailSubject;

    @Value("${forgot-password.email.message}")
    private String emailMessage;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private ResetPasswordTokenService tokenService;

    @Autowired
    private ResetPasswordTokenRepository tokenRepository;

    @Autowired
    private Environment env;

    @Test
    @SneakyThrows
    void shouldAssignAndSendTokenWhenUserDoesNotHavaOne() {
        var user = dbHelper.insertUser("john@gmail.com");

        var tokensBefore = tokenRepository.findAll();
        var mailsBefore = greenMail.getReceivedMessages();

        tokenService.assignAndSendToken(user.getEmail());

        var tokensAfter = tokenRepository.findAll();
        var mailsAfter = greenMail.getReceivedMessages();

        assertThat(tokensBefore).isEmpty();
        assertThat(mailsBefore).isEmpty();
        assertThat(tokensAfter).hasSize(1);
        assertThat(mailsAfter).hasSize(1);

        var token = tokensAfter.get(0);
        var mail = mailsAfter[0];

        assertThat(token.getToken()).isNotNull();
        assertThat(token.getExpiresIn()).isAfter(now(UTC));
        assertThat(token.getUser()).isEqualTo(user);

        assertThat(mail.getAllRecipients()).hasSize(1);
        assertThat(mail.getAllRecipients()[0].toString()).hasToString(user.getEmail());
        assertThat(mail.getSubject()).hasToString("CompreFace Reset Password");
        assertThat(GreenMailUtil.getBody(mail)).contains(buildMailBody(token.getToken()));
    }

    @Test
    @SneakyThrows
    void shouldReassignAndSendTokenWhenUserHasOne() {
        var user = dbHelper.insertUser("john@gmail.com");
        dbHelper.insertResetPasswordToken(user);

        var tokensBefore = tokenRepository.findAll();
        var mailsBefore = greenMail.getReceivedMessages();

        tokenService.assignAndSendToken(user.getEmail());

        var tokensAfter = tokenRepository.findAll();
        var mailsAfter = greenMail.getReceivedMessages();

        assertThat(tokensBefore).hasSize(1);
        assertThat(mailsBefore).isEmpty();
        assertThat(tokensAfter).hasSize(1);
        assertThat(mailsAfter).hasSize(1);

        var tokenBefore = tokensBefore.get(0);
        var tokenAfter = tokensAfter.get(0);
        var mail = mailsAfter[0];

        assertThat(tokenBefore.getToken()).isNotNull();
        assertThat(tokenBefore.getExpiresIn()).isAfter(now(UTC));
        assertThat(tokenBefore.getUser()).isEqualTo(user);
        assertThat(tokenAfter.getToken()).isNotNull();
        assertThat(tokenAfter.getExpiresIn()).isAfter(now(UTC));
        assertThat(tokenAfter.getUser()).isEqualTo(user);
        assertThat(tokenBefore.getToken()).isNotEqualByComparingTo(tokenAfter.getToken());

        assertThat(mail.getAllRecipients()).hasSize(1);
        assertThat(mail.getAllRecipients()[0].toString()).hasToString(user.getEmail());
        assertThat(mail.getSubject()).hasToString("CompreFace Reset Password");
        assertThat(GreenMailUtil.getBody(mail)).contains(buildMailBody(tokenAfter.getToken()));
    }

    @Test
    void shouldThrowUserDoesNotExistExceptionWhenThereIsNoActiveUserWithProvidedEmail() {
        var tokensBefore = tokenRepository.findAll();
        var mailsBefore = greenMail.getReceivedMessages();

        assertThatThrownBy(() -> tokenService.assignAndSendToken("john@gmail.com"))
                .isInstanceOf(UserDoesNotExistException.class)
                .hasMessageContaining("User john@gmail.com does not exist");

        var tokensAfter = tokenRepository.findAll();
        var mailsAfter = greenMail.getReceivedMessages();

        assertThat(tokensBefore).isEmpty();
        assertThat(mailsBefore).isEmpty();
        assertThat(tokensAfter).isEmpty();
        assertThat(mailsAfter).isEmpty();
    }

    private String buildMailBody(UUID token) {
        return String.format(emailMessage, env.getProperty("host.frs"), token.toString());
    }
}
