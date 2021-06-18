package com.spothero.parking.controller;

import com.spothero.parking.AbstractTest;
import java.time.ZonedDateTime;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class RatesControllerTests extends AbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void before() {
        deleteAll();
    }

    // ============================
    // = GET RATES TESTS
    // ============================
    @Test
    public void givenRequestForAllRatesShouldReturnAvailableRates() throws Exception {
        ratesService.setInitialRates();
        mockMvc.perform(get("/api/rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rates", hasSize(5)));
    }

    // ============================
    // = UPDATE RATES TESTS
    // ============================
    @Test
    public void givenANewSetOfInvalidTimezoneRatesShouldReturnError() throws Exception {
        String newRates = "{\n"
                + "    \"rates\": [{\n"
                + "            \"days\": \"mon,tues,thurs\",\n"
                + "            \"times\": \"0900-2100\",\n"
                + "            \"tz\": \"America/London\",\n"
                + "            \"price\": 1500\n"
                + "        }\n"
                + "    ]\n"
                + "}";

        mockMvc.perform(put("/api/rates")
                .content(newRates)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void givenANewSetOfRatesShouldOverriteAndReturnNewAvailableRates() throws Exception {
        ratesService.setInitialRates();
        mockMvc.perform(get("/api/rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rates", hasSize(5)));

        String newRates = "{\n"
                + "    \"rates\": [{\n"
                + "            \"days\": \"mon,tues,thurs\",\n"
                + "            \"times\": \"0900-2100\",\n"
                + "            \"tz\": \"America/Chicago\",\n"
                + "            \"price\": 1500\n"
                + "        }\n"
                + "    ]\n"
                + "}";

        mockMvc.perform(put("/api/rates")
                .content(newRates)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rates", hasSize(1)));
    }

    // ============================
    // = GET PRICES TEST
    // ============================
    @Test
    public void givenInvalidStartDateShouldReturnError() throws Exception {
        mockMvc.perform(get("/api/prices")
                .param("start", "")
                .param("end", ZonedDateTime.now().plusHours(1).toString()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void givenInvalidEndDateShouldReturnError() throws Exception {
        mockMvc.perform(get("/api/prices")
                .param("start", ZonedDateTime.now().toString())
                .param("end", ""))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void givenDateRangeMoreThan1DayShouldReturnUnavailable() throws Exception {
        mockMvc.perform(get("/api/prices")
                .param("start", "2015-07-04T15:00:00+00:00")
                .param("end", "2015-08-04T20:00:00+00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("unavailable")));
    }

    @Test
    public void givenValidDatesAndRatesMatchShouldReturnPrice() throws Exception {
        ratesService.setInitialRates();
        mockMvc.perform(get("/api/prices")
                .param("start", "2015-07-01T07:00:00-05:00")
                .param("end", "2015-07-01T12:00:00-05:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(1750)));
    }

    @Test
    public void givenValidDatesAndRatesMatchShouldReturnPrice2() throws Exception {
        ratesService.setInitialRates();
        mockMvc.perform(get("/api/prices")
                .param("start", "2015-07-04T15:00:00+00:00")
                .param("end", "2015-07-04T20:00:00+00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(2000)));
    }

    @Test
    public void givenValidDatesAndRatesDontMatchShouldReturnUnavailable() throws Exception {
        ratesService.setInitialRates();
        mockMvc.perform(get("/api/prices")
                .param("start", "2015-07-04T07:00:00+05:00")
                .param("end", "2015-07-04T20:00:00+05:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("unavailable")));
    }

}
