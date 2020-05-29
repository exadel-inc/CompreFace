package com.exadel.frs.core.trainservice.handler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.LOCKED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import com.exadel.frs.core.trainservice.dto.RetrainResponse;
import com.exadel.frs.core.trainservice.exception.ModelNotFoundException;
import com.exadel.frs.core.trainservice.exception.BadFormatModelKeyException;
import com.exadel.frs.core.trainservice.exception.MigrationExecutionException;
import com.exadel.frs.core.trainservice.exception.ModelAlreadyLockedException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {ModelAlreadyLockedException.class})
    public ResponseEntity<Object> handleModelAlreadyLocked(
            final RuntimeException ex
    ) {
        val modelAlreadyLockedException = (ModelAlreadyLockedException) ex;

        return ResponseEntity.status(LOCKED)
                             .body(new RetrainResponse(modelAlreadyLockedException.getMessage()));
    }

    @ExceptionHandler(value = {MigrationExecutionException.class})
    public ResponseEntity<Object> handleMigrationExecutionException(
            final RuntimeException ex
    ) {
        val migrationExecutionException = (MigrationExecutionException) ex;

        return ResponseEntity.status(BAD_REQUEST)
                             .body(new RetrainResponse(migrationExecutionException.getMessage()));
    }

    @ExceptionHandler(value = {BadFormatModelKeyException.class})
    public ResponseEntity<Object> handleBadFormatModelKeyException(final BadFormatModelKeyException e) {
        return ResponseEntity.status(BAD_REQUEST)
                             .body(new RetrainResponse(e.getMessage()));
    }

    @ExceptionHandler(value = {ModelNotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(final ModelNotFoundException e) {
        return ResponseEntity.status(NOT_FOUND)
                             .body(new RetrainResponse(e.getMessage()));
    }
}