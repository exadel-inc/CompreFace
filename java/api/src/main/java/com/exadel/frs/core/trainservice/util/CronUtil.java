package com.exadel.frs.core.trainservice.util;

import static com.cronutils.model.CronType.SPRING;
import static com.cronutils.model.definition.CronDefinitionBuilder.instanceDefinitionFor;
import static com.cronutils.model.time.ExecutionTime.forCron;
import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CronUtil {

    private static final CronParser cronParser;

    static {
        CronDefinition cronDefinition = instanceDefinitionFor(SPRING);
        cronParser = new CronParser(cronDefinition);
    }

    public static Optional<LocalDateTime> getSpecificExecutionBeforeNow(final String cronExpression, final int executionIndex) {
        ExecutionTime executionTime = forCron(cronParser.parse(cronExpression));
        ZonedDateTime currentExecution = now(UTC);

        for (int i = 0; i < executionIndex; i++) {
            Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(currentExecution);
            if (lastExecution.isPresent()) {
                currentExecution = lastExecution.get();
            } else {
                // there is no previous date
                return Optional.empty();
            }
        }

        return Optional.of(currentExecution.toLocalDateTime());
    }
}
