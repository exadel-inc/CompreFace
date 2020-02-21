package com.exadel.frs.validation;

import java.util.regex.Pattern;

public class EmailValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    public static boolean isValid(final String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isInvalid(final String email) {
        return !isValid(email);
    }
}