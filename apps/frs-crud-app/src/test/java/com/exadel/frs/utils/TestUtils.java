package com.exadel.frs.utils;

import com.exadel.frs.dto.ExceptionResponseDto;
import com.exadel.frs.entity.User;
import com.exadel.frs.exception.BasicException;
import com.exadel.frs.handler.ExceptionCode;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class TestUtils {

    public static final long USER_ID = 3L;

    public static ExceptionResponseDto buildExceptionResponse(final BasicException ex) {
        return ExceptionResponseDto.builder()
                .code(ex.getExceptionCode().getCode())
                .message(ex.getMessage())
                .build();
    }

    public static ExceptionResponseDto buildUndefinedExceptionResponse(final Exception ex) {
        return ExceptionResponseDto.builder()
                .code(ExceptionCode.UNDEFINED.getCode())
                .message(ex.getMessage())
                .build();
    }

    public static User buildUser() {
        return User.builder()
                .id(USER_ID)
                .firstName(randomAlphabetic(10))
                .lastName(randomAlphabetic(10))
                .email(randomAlphabetic(10) + "@" + randomAlphabetic(5) + ".com")
                .build();
    }
}