package com.spothero.parking;

import com.spothero.parking.domains.rate.RatesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AppEvents {

    @Autowired
    private RatesService ratesService;

    @EventListener(ApplicationReadyEvent.class)
    public void startSuccess() {
        log.info("Started Application");
        ratesService.setInitialRates();
    }

}
