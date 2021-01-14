package com.exadel.frs.scheduler.config;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import com.exadel.frs.scheduler.job.StatisticsJob;
import lombok.AllArgsConstructor;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class Configuration {

    @Bean
    public Scheduler schedule() throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();

        JobDetail job = JobBuilder.newJob(StatisticsJob.class)
                                  .build();

        scheduler.scheduleJob(job, statisticsTrigger());
        scheduler.start();
        return scheduler;
    }

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
