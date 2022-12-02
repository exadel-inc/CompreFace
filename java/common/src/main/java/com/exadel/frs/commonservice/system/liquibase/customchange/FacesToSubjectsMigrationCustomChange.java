package com.exadel.frs.commonservice.system.liquibase.customchange;

import com.exadel.frs.commonservice.system.liquibase.FacesToSubjectMigrationProcessor;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.integration.spring.SpringResourceAccessor;
import liquibase.resource.ResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.util.Optional;

@Slf4j
public class FacesToSubjectsMigrationCustomChange implements CustomTaskChange {

    private ResourceAccessor resourceAccessor;
    private FacesToSubjectMigrationProcessor processor;

    @Override
    public void execute(Database database) throws CustomChangeException {
        long start = System.currentTimeMillis();

        int migrated = processor.start();

        log.info("Migrated {} faces in {}ms", migrated, (System.currentTimeMillis() - start));
    }

    @Override
    public String getConfirmationMessage() {
        return "ok";
    }

    @Override
    public void setUp() throws SetupException {
        var applicationContext = Optional
                .ofNullable(ReflectionUtils.findField(SpringResourceAccessor.class, "resourceLoader"))
                .stream()
                .peek(ReflectionUtils::makeAccessible)
                .findFirst()
                .map(field -> ReflectionUtils.getField(field, resourceAccessor))
                .filter(ApplicationContext.class::isInstance)
                .map(ApplicationContext.class::cast)
                .orElseThrow(() -> new IllegalStateException("Unable to load application context to perform migration"));

        this.processor = applicationContext.getBean(FacesToSubjectMigrationProcessor.class);
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }
}
