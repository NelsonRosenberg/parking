package com.spothero.parking;

import com.spothero.parking.domains.cache.RatesCacheService;
import com.spothero.parking.domains.rate.RatesRepository;
import com.spothero.parking.domains.rate.RatesService;
import javax.transaction.Transactional;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class})
public abstract class AbstractTest {

    @Autowired
    public RatesCacheService ratesCacheService;

    @Autowired
    public RatesRepository ratesRepository;

    @Autowired
    public RatesService ratesService;

    @Transactional
    public void deleteAll() {
        ratesRepository.deleteAll();
        ratesCacheService.clearCache();
    }

}
