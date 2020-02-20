package com.exadel.frs.utils;

import com.exadel.frs.dto.ExceptionResponseDto;
import com.exadel.frs.entity.User;
import com.exadel.frs.exception.BasicException;
import com.exadel.frs.handler.ExceptionCode;

public class TestUtils {

    public static final long USER_ID = 3L;
    public static final String USERNAME = "test";

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

    public static User buildDefaultUser() {
        return User.builder().email(USERNAME).id(USER_ID).build();
    }
}
