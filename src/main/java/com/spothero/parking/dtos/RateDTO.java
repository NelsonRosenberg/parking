package com.spothero.parking.dtos;

import com.spothero.parking.dtos.validations.*;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Range")
public class RateDTO {

    @NotBlank
    private String days;

    @NotBlank
    @Pattern(regexp = "^[\\d]{4}-[\\d]{4}$", message = "Plese use format 0000-0000")
    private String times;

    @NotBlank
    @TimeZoneFormat
    private String tz;

    @NotNull
    private Integer price;

}
