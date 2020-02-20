package com.exadel.frs.validation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {
    private static List<String> VALID_EMAILS;
    private static List<String> INVALID_EMAILS;

    @BeforeAll
    public static void init() {
        VALID_EMAILS = List.of(
                "tester@exadel.com",
                "tester.100@exadel.com",
                "tester123@tester.com",
                "abc@3.com",
                "tester-9000@exadel.com",
                "tester-5500@tester.net",
                "tester.100@tester.com.au",
                "tester@gmail.com.com",
                "tester+100@gmail.com",
                "tester-100@exadel-test.com",
                "valijon@sobirov.uz"
        );

        INVALID_EMAILS = List.of(
                "tester",
                "tester@.com.my",
                "tester123@gmail.a",
                "tester123@.com",
                "tester123@.com.com",
                ".tester@tester.com",
                "tester()*@gmail.com",
                "tester@%*.com",
                "tester..2002@gmail.com",
                "tester.@gmail.com",
                "tester@tester@gmail.com",
                "tester@gmail.com.1a",
                "",
                "   "
        );
    }

    @Test
    void validEmailsPassValidation() {
        VALID_EMAILS.forEach(email -> assertTrue(EmailValidator.isValid(email)));
        VALID_EMAILS.forEach(email -> assertFalse(EmailValidator.isInvalid(email)));
    }

    @Test
    void invalidEmailsFailValidation() {
        INVALID_EMAILS.forEach(email -> assertFalse(EmailValidator.isValid(email)));
        INVALID_EMAILS.forEach(email -> assertTrue(EmailValidator.isInvalid(email)));
    }
}