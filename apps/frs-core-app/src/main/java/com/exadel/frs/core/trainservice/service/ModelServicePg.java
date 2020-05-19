package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.enums.ValidationResult;
import com.exadel.frs.core.trainservice.repository.postgres.ModelRepositoryPg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModelServicePg {

    private final ModelRepositoryPg modelRepositoryPg;

    @Cacheable("modelKeys")
    public ValidationResult validateModelKey(final String modelKey) {
        if (modelRepositoryPg.findByApiKey(modelKey).isPresent()) {
            return ValidationResult.OK;
        }
        return ValidationResult.FORBIDDEN;
    }
}
