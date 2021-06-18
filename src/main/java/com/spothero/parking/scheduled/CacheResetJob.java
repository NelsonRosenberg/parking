package com.spothero.parking.scheduled;

import com.spothero.parking.domains.rate.RatesService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class CacheResetJob implements Job {

    @Autowired
    private RatesService ratesService;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Reseting cache.");
        ratesService.resetCache();
    }
}
