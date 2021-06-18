package com.spothero.parking.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Parking")
public class ParkingDTO {

    @NotEmpty
    @Builder.Default
    List<@Valid RateDTO> rates = new ArrayList<>();
}
