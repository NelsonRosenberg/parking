package com.spothero.parking.domains.rate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spothero.parking.domains.cache.RatesCached;
import com.spothero.parking.domains.cache.RatesCacheService;
import com.spothero.parking.dtos.ParkingDTO;
import com.spothero.parking.dtos.RateDTO;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RatesService {

    @Value("classpath:data/rates.json")
    private Resource ratesJson;

    @Autowired
    private RatesRepository rateRepository;

    @Autowired
    private RatesCacheService ratesCacheService;

    private static final String UNAVAILABLE = "\"unavailable\"";
    private static final String PRICE_JSON = "{\"price\": number}";
    private static final String PRICE_REPLACE = "number";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");

    // ============================
    // = INITIALIZE METHODS
    // ============================
    @Transactional
    public void setInitialRates() {
        if (ratesJson != null) {
            log.info("Initializing rates from file.");
            try (Reader in = new InputStreamReader(ratesJson.getInputStream())) {
                ParkingDTO rates = objectMapper.readValue(in, ParkingDTO.class);
                saveRates(rates);
            } catch (Exception ex) {
                log.error("Failed to initialize rates.", ex);
            }
            log.info("Finished initializing rates from file.");
        }
    }

    // ============================
    // = RESET CACHE METHODS
    // ============================
    public void resetCache() {
        ratesCacheService.clearCache();
        getMappedRates();
    }

    // ============================
    // = GET RATES METHODS
    // ============================
    public ParkingDTO getRatesInfo() {
        // Get from DB
        List<Rate> rates = rateRepository.findAll();

        List<RateDTO> ratesDTO = new ArrayList<>();
        if (rates != null && !rates.isEmpty()) {
            for (Rate rate : rates) {
                RateDTO rateDTO = RateDTO
                        .builder()
                        .days(StringUtils.join(rate.getDays(), ','))
                        .times(rate.getStartTime() + "-" + rate.getEndTime())
                        .tz(rate.getTz())
                        .price(rate.getPrice())
                        .build();

                ratesDTO.add(rateDTO);
            }
        }

        return new ParkingDTO(ratesDTO);
    }

    // ============================
    // = UPDATE RATES METHODS
    // ============================
    @Transactional
    public ParkingDTO saveRates(ParkingDTO parkingDTO) {
        // Delete all existing rates from DB and cache
        ratesCacheService.clearCache();
        rateRepository.deleteAll();

        // Save new rates
        List<Rate> newRates = new ArrayList<>();
        for (RateDTO newRate : parkingDTO.getRates()) {
            String[] times = newRate.getTimes().trim().split("-");
            Rate rate = Rate
                    .builder()
                    .days(Arrays.asList(newRate.getDays().trim().split(",")))
                    .startTime(times[0])
                    .endTime(times[1])
                    .tz(newRate.getTz())
                    .price(newRate.getPrice())
                    .build();

            newRates.add(rate);
        }
        rateRepository.saveAll(newRates);

        // Update cache
        ratesCacheService.addToCache(mapRates(newRates));

        return parkingDTO;
    }

    // ============================
    // = GET PRICE METHODS
    // ============================
    public String getPrice(String startStr, String endStr) {
        // Parse string accounting for spaces(fake + signs) and %2b (real + signs)
        ZonedDateTime start = ZonedDateTime.parse(getCorrectDateString(startStr), formatter);
        ZonedDateTime end = ZonedDateTime.parse(getCorrectDateString(endStr), formatter);

        // Validate if request does not span more than a day
        if (!start.toLocalDate().isEqual(end.toLocalDate())) {
            return UNAVAILABLE;
        }

        // Since we have to check all rates, we map the entries by timezone and then by day
        // By mapping by timezone, it allows us to not have to do many timezone calculations
        // After we change the requested dates to the timezone, we can then safely get the rates by day of week
        // If the day of week matches, we can then test the time and get a final match for the price
        RatesCached rateCached = getMappedRates();
        List<Rate> matches = new ArrayList<>();
        for (Map.Entry<String, Map<String, Set<Long>>> tzEntry : rateCached.getCalculatedMap().entrySet()) {
            String tz = tzEntry.getKey();
            Map<String, Set<Long>> daysMap = tzEntry.getValue();

            // Get request variables with new timezone from rate
            ZonedDateTime startAtNewTZ = start.withZoneSameInstant(ZoneId.of(tz));
            ZonedDateTime endAtNewTZ = end.withZoneSameInstant(ZoneId.of(tz));

            // Validate if request does not span more than a day, as it can happen after the timezone change
            if (!startAtNewTZ.toLocalDate().isEqual(endAtNewTZ.toLocalDate())) {
                return UNAVAILABLE;
            }

            // Get the calculation variables
            String dayOfWeekAtNewTZ = startAtNewTZ.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US).toLowerCase();

            // Calculate rates for that day
            if (daysMap.containsKey(dayOfWeekAtNewTZ)) {
                Integer startTime = Integer.valueOf(get4CharTime(startAtNewTZ.getHour()) + get4CharTime(startAtNewTZ.getMinute()));
                Integer endTime = Integer.valueOf(get4CharTime(endAtNewTZ.getHour()) + get4CharTime(endAtNewTZ.getMinute()));

                for (Long rateId : daysMap.get(dayOfWeekAtNewTZ)) {
                    Rate rate = rateCached.getRateMap().get(rateId);

                    if (startTime >= Integer.valueOf(rate.getStartTime())
                            && endTime <= Integer.valueOf(rate.getEndTime())) {

                        matches.add(rate);
                    }
                }
            }

            // If we get more than one match, we can stop and return unavailable
            if (matches.size() > 1) {
                return UNAVAILABLE;
            }
        }

        if (matches.isEmpty()) {
            return UNAVAILABLE;
        } else {
            return PRICE_JSON.replace(PRICE_REPLACE, String.valueOf(matches.get(0).getPrice()));
        }
    }

    private RatesCached getMappedRates() {
        RatesCached rates = ratesCacheService.getAllFromCache();
        if (rates == null) {
            rates = mapRates(rateRepository.findAll());
            ratesCacheService.addToCache(rates);
        }
        return rates;
    }

    private RatesCached mapRates(List<Rate> rates) {
        RatesCached rateCached = new RatesCached();
        Map<String, Map<String, Set<Long>>> calculatedMap = new HashMap<>();
        Map<Long, Rate> rateMap = new HashMap<>();

        for (Rate rate : rates) {
            rateMap.put(rate.getId(), rate);

            String tz = rate.getTz();
            Map<String, Set<Long>> daysMap;
            if (calculatedMap.containsKey(tz)) {
                daysMap = calculatedMap.get(tz);
            } else {
                daysMap = new HashMap<>();
            }

            for (String day : rate.getDays()) {
                if (daysMap.containsKey(day)) {
                    daysMap.get(day).add(rate.getId());
                } else {
                    Set<Long> rateIdSet = new HashSet<>();
                    rateIdSet.add(rate.getId());
                    daysMap.put(day, rateIdSet);
                }
            }

            calculatedMap.put(tz, daysMap);
        }

        rateCached.setCalculatedMap(calculatedMap);
        rateCached.setRateMap(rateMap);
        return rateCached;
    }

    private String get4CharTime(int time) {
        return time < 10 ? "0" + String.valueOf(time) : String.valueOf(time);
    }

    private String getCorrectDateString(String dateStr) {
        return dateStr.trim().replace(" ", "+").replace("%2b", "+");
    }

}
