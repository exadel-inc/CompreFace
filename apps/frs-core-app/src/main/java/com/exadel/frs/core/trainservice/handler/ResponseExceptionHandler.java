package com.exadel.frs.core.trainservice.handler;

import com.exadel.frs.core.trainservice.dto.RetrainResponse;
import com.exadel.frs.core.trainservice.exception.ModelAlreadyLockedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class ResponseExceptionHandler
    extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value
      = {ModelAlreadyLockedException.class})
  protected ResponseEntity<Object> handleModelAlreadyLocked(
      RuntimeException ex, WebRequest request) {
    ModelAlreadyLockedException modelAlreadyLockedException = (ModelAlreadyLockedException) ex;
    return ResponseEntity.status(HttpStatus.LOCKED)
        .body(new RetrainResponse(modelAlreadyLockedException.getMessage()));
  }
}