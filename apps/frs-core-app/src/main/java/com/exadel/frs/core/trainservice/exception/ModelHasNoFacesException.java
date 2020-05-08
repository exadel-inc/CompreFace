package com.exadel.frs.core.trainservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Model has no faces")
public class ModelHasNoFacesException extends RuntimeException {

}