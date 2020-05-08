package com.exadel.frs.core.trainservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Classifier doesn't exist")
public class ClassifierNotTrained extends RuntimeException {

}