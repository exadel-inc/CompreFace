package com.exadel.frs.core.trainservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.LOCKED, reason = "Classifier training is already in progress")
public class ClassifierIsAlreadyTrainingException extends RuntimeException {

}
