package com.exadel.frs.validation;

import java.util.regex.Pattern;

public class EmailValidator {

    private static final Pattern VALID_EMAIL_PATTERN;
    private static final String EMAIL_REGEX;

    static {
        EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        VALID_EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    }


    public static boolean isValid(final String email) {
        return VALID_EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isInvalid(final String email) {
        return !isValid(email);
    }
}