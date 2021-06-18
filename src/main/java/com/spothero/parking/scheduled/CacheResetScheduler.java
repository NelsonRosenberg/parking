package com.spothero.parking.scheduled;

import java.util.TimeZone;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value = "cache.reset.enable",
        havingValue = "true",
        matchIfMissing = true)
public class CacheResetScheduler {

    @Value("${cache.reset.schedule}")
    private String cacheResetSchedule;

    @Value("${parking.timezone}")
    private String timezone;

    @Bean(name = "cacheResetDetails")
    public JobDetail cacheResetDetails() {
        return JobBuilder
                .newJob(CacheResetJob.class)
                .withIdentity("cacheResetDetails")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger cacheResetTrigger(JobDetail cacheResetDetails) {
        return TriggerBuilder.newTrigger()
                .forJob(cacheResetDetails)
                .withIdentity("cacheResetTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(cacheResetSchedule)
                        .inTimeZone(TimeZone.getTimeZone(timezone)))
                .build();
    }

}
