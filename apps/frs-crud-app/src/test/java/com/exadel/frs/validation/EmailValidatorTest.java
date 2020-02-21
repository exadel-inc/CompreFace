package com.exadel.frs.validation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.exadel.frs.validation.EmailValidator.isInvalid;
import static com.exadel.frs.validation.EmailValidator.isValid;
import static org.assertj.core.api.Assertions.assertThat;

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
    void validEmailsAreValid() {
        assertThat(VALID_EMAILS).allSatisfy(
                email -> assertThat(isValid(email)).isTrue()
        );
    }

    @Test
    void invalidEmailsAreNotValid() {
        assertThat(INVALID_EMAILS).allSatisfy(
                email -> assertThat(isValid(email)).isFalse()
        );
    }

    @Test
    void invalidEmailsAreInvalid() {
        assertThat(INVALID_EMAILS).allSatisfy(
                email -> assertThat(isInvalid(email)).isTrue()
        );
    }

    @Test
    void validEmailsAreNotInvalid() {
        assertThat(VALID_EMAILS).allSatisfy(
                email -> assertThat(isInvalid(email)).isFalse()
        );
    }
}