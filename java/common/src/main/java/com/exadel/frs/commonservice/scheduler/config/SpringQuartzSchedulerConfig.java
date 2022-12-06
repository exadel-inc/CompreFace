package com.exadel.frs.commonservice.scheduler.config;

import com.exadel.frs.commonservice.scheduler.job.StatisticsJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class SpringQuartzSchedulerConfig {

    @Bean
    JobDetail jobDetail() {
        return JobBuilder.newJob(StatisticsJob.class)
                         .withIdentity("StatisticsJob")
                         .storeDurably()
                         .build();
    }

    @Bean
    Trigger trigger() {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder
                .newTrigger()
                .forJob(jobDetail())
                .withIdentity("Statistics trigger");
        triggerBuilder
                .withSchedule(SimpleScheduleBuilder
                        .simpleSchedule()
                        .withIntervalInHours(24)
                        .repeatForever())
                .startNow();
        return triggerBuilder.build();
    }

    @Bean
    public Scheduler scheduler(Trigger trigger, JobDetail job, SchedulerFactoryBean factory) throws SchedulerException {
        Scheduler scheduler = factory.getScheduler();
        scheduler.start();
        return scheduler;
    }
}
