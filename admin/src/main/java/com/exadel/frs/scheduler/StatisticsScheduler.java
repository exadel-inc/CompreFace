package com.exadel.frs.scheduler;

import com.exadel.frs.scheduler.job.StatisticsJob;
import lombok.AllArgsConstructor;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StatisticsScheduler {

    Trigger statisticsTrigger;

    @Bean
    public Scheduler schedule() throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();

        JobDetail job = JobBuilder.newJob(StatisticsJob.class)
                                  .build();

        scheduler.scheduleJob(job, statisticsTrigger);
        scheduler.start();
        return scheduler;
    }
}
