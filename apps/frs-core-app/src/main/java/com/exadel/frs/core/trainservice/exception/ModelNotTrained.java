package com.exadel.frs.core.trainservice.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = BAD_REQUEST, reason = "Model is not trained")
public class ModelNotTrained extends RuntimeException {

}