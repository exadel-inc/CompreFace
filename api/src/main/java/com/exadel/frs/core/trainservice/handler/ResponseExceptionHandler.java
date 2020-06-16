/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.core.trainservice.handler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.LOCKED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import com.exadel.frs.core.trainservice.dto.RetrainResponse;
import com.exadel.frs.core.trainservice.exception.BadFormatModelKeyException;
import com.exadel.frs.core.trainservice.exception.MigrationExecutionException;
import com.exadel.frs.core.trainservice.exception.ModelAlreadyLockedException;
import com.exadel.frs.core.trainservice.exception.ModelNotFoundException;
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