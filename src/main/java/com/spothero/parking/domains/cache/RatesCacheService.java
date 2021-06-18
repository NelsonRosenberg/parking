package com.spothero.parking.domains.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RatesCacheService {

    @Value("${cache.redis.key}")
    private String KEY;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOps;

    @Autowired
    private ObjectMapper mapper;

    public RatesCached getAllFromCache() {
        RatesCached rates = null;
        try {
            String cache = valueOps.get(KEY);
            if (cache != null && !cache.isBlank()) {
                rates = mapper.readValue(cache, RatesCached.class);
            }
        } catch (Exception ex) {
            log.error("Error when accessing cache.", ex);
        }

        return rates;
    }

    public void addToCache(RatesCached rates) {
        try {
            if (rates != null) {
                valueOps.set(KEY, mapper.writeValueAsString(rates));
            }
        } catch (Exception ex) {
            log.error("Error when adding to cache.", ex);
        }
    }

    public void clearCache() {
        try {
            valueOps.set(KEY, "");
        } catch (Exception ex) {
            log.error("Error when clearing cache.", ex);
        }
    }

}
