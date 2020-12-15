package com.exadel.frs.helpers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.exception.UnreachableEmailException;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class EmailSenderTest {

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @InjectMocks
    private EmailSender emailSender;

    @Mock
    private JavaMailSender javaMailSenderMock;

    @Test
    void sendMailSenderCatchMailException() {
        when(javaMailSenderMock.createMimeMessage()).thenReturn(mock(MimeMessage.class));
        doThrow(new MailSendException("error")).when(javaMailSenderMock).send(any(MimeMessage.class));

        assertThatThrownBy(() -> {
            emailSender.sendMail("test@email.com", "subject", "msg");
        }).isInstanceOf(UnreachableEmailException.class);
    }
}
