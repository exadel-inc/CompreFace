package com.exadel.frs.handler;

import static com.exadel.frs.handler.ExceptionCode.UNDEFINED;
import com.exadel.frs.dto.ExceptionResponseDto;
import com.exadel.frs.exception.BasicException;
import com.exadel.frs.exception.ConstraintViolationException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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

    private BasicException getException(final FieldError fieldError) {
        BasicException basicException;

        switch (fieldError.getCode()) {
            case "NotNull":
            case "NotBlank":
            case "NotEmpty":
                basicException = new EmptyRequiredFieldException(fieldError.getField());
                break;
            case "Size":
                basicException = new ConstraintViolationException(fieldError.getField(), fieldError.getDefaultMessage());
                break;
            default:
                basicException = new BasicException(UNDEFINED, "");
        }

        return basicException;
    }

    private ResponseEntity<ExceptionResponseDto> buildBody(final FieldError fieldError) {
        val exception = getException(fieldError);

        return handleDefinedExceptions(exception);
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
