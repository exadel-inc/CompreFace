package com.exadel.frs.handler;

import com.exadel.frs.dto.ExceptionResponseDto;
import com.exadel.frs.exception.BasicException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.exadel.frs.handler.ExceptionCode.EMPTY_REQUIRED_FIELD;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDto> handleInvalidMethodArguments(final MethodArgumentNotValidException ex) {
        log.error("Field validation failed", ex);

        val fieldError = ex.getBindingResult().getFieldErrors().get(0);

        return buildBody(fieldError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleUndefinedExceptions(final Exception ex) {
        log.error("Undefined exception occurred", ex);

        return ResponseEntity.status(UNDEFINED.getHttpStatus()).body(buildBody());
    }

    private ExceptionCode getExceptionCode(final FieldError fieldError) {
        ExceptionCode exceptionCode;

        switch (fieldError.getCode()) {
            case "NotBlank":
                exceptionCode = EMPTY_REQUIRED_FIELD;
                break;
            default:
                exceptionCode = UNDEFINED;
        }

        return exceptionCode;
    }

    private ResponseEntity<ExceptionResponseDto> buildBody(final FieldError fieldError) {
        val exceptionCode = getExceptionCode(fieldError);

        return ResponseEntity
                .status(exceptionCode.getHttpStatus())
                .body(ExceptionResponseDto.builder()
                        .code(exceptionCode.getCode())
                        .message(fieldError.getDefaultMessage())
                        .build());
    }

    private ExceptionResponseDto buildBody(final BasicException ex) {
        return ExceptionResponseDto.builder()
                .code(ex.getExceptionCode().getCode())
                .message(ex.getMessage())
                .build();
    }

    private ExceptionResponseDto buildBody() {
        return ExceptionResponseDto.builder()
                .code(UNDEFINED.getCode())
                .message("Something went wrong, please try again")
                .build();
    }
}
