package com.exadel.frs.handler;

import com.exadel.frs.dto.ExceptionResponseDto;
import com.exadel.frs.exception.BasicException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.exadel.frs.handler.ExceptionCode.UNDEFINED;

@ControllerAdvice
@Slf4j
public class ResponseExceptionHandler {

    @ExceptionHandler(BasicException.class)
    public ResponseEntity<ExceptionResponseDto> handleDefinedExceptions(final BasicException ex) {
        log.error("Defined exception occurred", ex);

        return ResponseEntity
                     .status(ex.getExceptionCode().getHttpStatus())
                     .body(buildBody(ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleUndefinedExceptions(final Exception ex) {
        log.error("Undefined exception occurred", ex);

        return ResponseEntity.status(UNDEFINED.getHttpStatus()).body(buildBody(ex));
    }

    private ExceptionResponseDto buildBody(final BasicException ex) {
        return ExceptionResponseDto.builder()
                                   .code(ex.getExceptionCode().getCode())
                                   .message(ex.getMessage())
                                   .build();
    }

    private ExceptionResponseDto buildBody(final Exception ex) {
        return ExceptionResponseDto.builder()
                                   .code(UNDEFINED.getCode())
                                   .message("Something went wrong, please try again")
                                   .build();
    }
}
