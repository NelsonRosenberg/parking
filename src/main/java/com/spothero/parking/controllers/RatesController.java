package com.spothero.parking.controllers;

import com.spothero.parking.domains.rate.RatesService;
import com.spothero.parking.dtos.ParkingDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/api")
public class RatesController {

    @Autowired
    private RatesService rateService;

    @Operation(summary = "Retrieve rates.")
    @GetMapping(path = "/rates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParkingDTO getRatesInfo() {
        return rateService.getRatesInfo();
    }

    @Operation(summary = "Update rates.")
    @PutMapping(path = "/rates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParkingDTO updateRatesInfo(@RequestBody @Parameter(name = "Parking") @Valid ParkingDTO parkingDTO) {
        return rateService.saveRates(parkingDTO);
    }

    @Operation(summary = "Get price for dates.")
    @GetMapping(path = "/prices", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getPrice(
            @RequestParam(name = "start", required = true)
            @Parameter(name = "start", example = "2011-12-03T10:15:30+01:00") @NotBlank String start,
            @RequestParam(name = "end", required = true)
            @Parameter(name = "end", example = "2011-12-03T10:15:30+01:00") @NotBlank String end) {

        return rateService.getPrice(start, end);
    }

}
