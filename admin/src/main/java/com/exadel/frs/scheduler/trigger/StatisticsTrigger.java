package com.exadel.frs.scheduler.trigger;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class StatisticsTrigger {

    @Bean
    public Trigger statisticsTrigger() {
        return TriggerBuilder.newTrigger()
                             .startNow()
//                             .startAt(Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                             .withSchedule(simpleSchedule()
                                     .withIntervalInHours(24)
                                     .repeatForever())
                             .build();
    }
}
