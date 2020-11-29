package com.exadel.frs.helpers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.exception.UnreachableEmailException;
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
        doThrow(new MailSendException("error")).when(javaMailSenderMock).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> {
            emailSender.sendMail("test@email.com", "subject", "msg");
        }).isInstanceOf(UnreachableEmailException.class);
    }
}
