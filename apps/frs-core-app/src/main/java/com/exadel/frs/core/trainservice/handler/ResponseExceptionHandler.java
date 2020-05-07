package com.exadel.frs.core.trainservice.handler;

import com.exadel.frs.core.trainservice.dto.RetrainResponse;
import com.exadel.frs.core.trainservice.exception.MigrationExecutionException;
import com.exadel.frs.core.trainservice.exception.ModelAlreadyLockedException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {ModelAlreadyLockedException.class})
    protected ResponseEntity<Object> handleModelAlreadyLocked(
            final RuntimeException ex,
            final WebRequest request
    ) {
        val modelAlreadyLockedException = (ModelAlreadyLockedException) ex;

        return ResponseEntity.status(HttpStatus.LOCKED)
                             .body(new RetrainResponse(modelAlreadyLockedException.getMessage()));
    }

    @ExceptionHandler(value = {MigrationExecutionException.class})
    protected ResponseEntity<Object> handleMigrationExecutionException(
            final RuntimeException ex,
            final WebRequest request
    ) {
        val migrationExecutionException = (MigrationExecutionException) ex;

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RetrainResponse(migrationExecutionException.getMessage()));
    }

}