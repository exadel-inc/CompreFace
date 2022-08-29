package com.exadel.frs.core.trainservice.util;

import static com.cronutils.model.CronType.SPRING;
import static com.cronutils.model.definition.CronDefinitionBuilder.instanceDefinitionFor;
import static com.cronutils.model.time.ExecutionTime.forCron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import java.time.ZonedDateTime;
import java.util.Optional;

public class CronExecution {

    private final ExecutionTime executionTime;

    public CronExecution(final String cronExpression) {
        CronDefinition cronDefinition = instanceDefinitionFor(SPRING);
        CronParser cronParser = new CronParser(cronDefinition);
        executionTime = forCron(cronParser.parse(cronExpression));
    }

    public Optional<ZonedDateTime> getLastExecutionBefore(final ZonedDateTime date) {
        return executionTime.lastExecution(date);
    }
}
