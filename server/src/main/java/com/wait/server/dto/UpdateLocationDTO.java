package com.wait.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateLocationDTO {
    @NotNull
    private BigDecimal lng;

    @NotNull
    private BigDecimal lat;
}
