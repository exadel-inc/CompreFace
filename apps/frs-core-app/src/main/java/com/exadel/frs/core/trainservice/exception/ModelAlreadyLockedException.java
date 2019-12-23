package com.exadel.frs.core.trainservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelAlreadyLockedException extends RuntimeException {
  private String message;
}
