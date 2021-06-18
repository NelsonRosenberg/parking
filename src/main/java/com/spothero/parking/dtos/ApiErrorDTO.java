package com.spothero.parking.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Api-Error")
public class ApiErrorDTO {

    private LocalDateTime timestamp = LocalDateTime.now();
    private String errorMessage;
    private HttpStatus status;

    public ApiErrorDTO(String message, HttpStatus status) {
        this.status = status;
        this.errorMessage = message;
    }

    public ApiErrorDTO(Exception ex, HttpStatus status) {
        this.status = status;
        this.errorMessage = ex.getMessage() != null ? ex.getMessage().split(";")[0] : "Server error";
    }

}
